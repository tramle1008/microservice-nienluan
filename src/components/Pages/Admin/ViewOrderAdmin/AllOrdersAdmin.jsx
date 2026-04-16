// src/pages/admin/AllOrdersAdmin.jsx
import { useEffect, useState } from "react";
import api from "../../../../api/api"; // api đã có interceptor token
import AdminOrderPagination from "../AdminOrderPagination";
import AdminSidebar from "../AdminSidebar";
import ViewOrderDetailButton from "./ViewOrderDetailButton";
import HandleDelivered from "../../Order_Admin/HandleDelivered";
import { useSearchParams } from "react-router-dom";

const statusColors = {
    PENDING: "bg-yellow-100 text-yellow-800",
    CONFIRMED: "bg-blue-100 text-blue-800",
    SHIPPED: "bg-purple-100 text-purple-800",
    DELIVERED: "bg-green-100 text-green-800",
    CANCELLED: "bg-red-100 text-red-800",
};

const statusLabels = {
    PENDING: "Chờ xác nhận",
    CONFIRMED: "Đã xác nhận",
    SHIPPED: "Đang giao",
    DELIVERED: "Đã giao",
    CANCELLED: "Đã hủy",
};

const AllOrdersAdmin = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    const [totalAmount, settotalAmount] = useState(0);
    // State
    const [orders, setOrders] = useState([]);
    const [totalPages, setTotalPages] = useState(0);
    const [loading, setLoading] = useState(true);
    const getToken = () => JSON.parse(localStorage.getItem("auth"))?.jwtToken;
    // State tìm kiếm
    const [searchTerm, setSearchTerm] = useState("");

    // URL params
    const page = parseInt(searchParams.get("page") ?? "0", 10);
    const size = parseInt(searchParams.get("size") ?? "3", 10);
    const statusFilter = searchParams.get("status") || "";

    // Gọi API
    const fetchAllOrders = async () => {
        setLoading(true);
        try {
            const params = new URLSearchParams({
                page: page.toString(),
                size: size.toString(),
                sortBy: "orderDate",
                sortDir: "desc",
            });

            if (statusFilter) params.append("status", statusFilter);
            if (searchTerm.trim()) params.append("search", searchTerm.trim());

            if (searchParams.get("fromDate")) {
                params.append("fromDate", searchParams.get("fromDate"));
            }
            if (searchParams.get("toDate")) {
                params.append("toDate", searchParams.get("toDate"));
            }

            const token = getToken();
            const res = await api.get(`/orders/admin/all?${params.toString()}`,

                {
                    headers: {
                        'Authorization': token ? `Bearer ${token}` : '',
                    }
                }
            );
            setOrders(res.data.content || []);
            setTotalPages(res.data.totalPages || 1);
            settotalAmount(res.data.totalAmount)
        } catch (err) {
            console.error("Lỗi tải đơn hàng:", err);
            setOrders([]);
        } finally {
            setLoading(false);
        }
    };

    const fromDate = searchParams.get("fromDate");
    const toDate = searchParams.get("toDate");

    useEffect(() => {
        fetchAllOrders();
    }, [page, statusFilter, searchTerm, fromDate, toDate]);

    // Xử lý thay đổi bộ lọc trạng thái
    const handleStatusChange = (e) => {
        const value = e.target.value;
        setSearchParams((prev) => {
            const newParams = new URLSearchParams(prev);
            if (value === "") newParams.delete("status");
            else newParams.set("status", value);
            newParams.set("page", "0");
            return newParams;
        });
    };

    // Xử lý Enter để tìm luôn
    const handleKeyDown = (e) => {
        if (e.key === "Enter") {
            setSearchParams((prev) => {
                const newParams = new URLSearchParams(prev);
                newParams.set("page", "0");
                return newParams;
            });
        }
    };

    return (
        <div className="flex min-h-screen bg-gray-100">
            <AdminSidebar />

            <main className="flex-1 p-8">
                <div className="max-w-7xl mx-auto">
                    <br /><br />
                    <h1 className="text-3xl font-bold text-gray-800 mb-6">Tất cả đơn hàng</h1>

                    <div className="mb-6 flex flex-col sm:flex-row gap-4 flex-wrap items-end">
                        {/* Ô tìm kiếm */}
                        <div className="flex-1 min-w-64">
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Tìm kiếm mã đơn / ID
                            </label>
                            <input
                                type="text"
                                value={searchTerm}
                                onChange={(e) => setSearchTerm(e.target.value)}
                                onKeyDown={handleKeyDown}
                                placeholder="VD: DH1766120441803 hoặc 12345"
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                            />
                        </div>

                        {/* Từ ngày */}
                        <div className="min-w-48">
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Từ ngày
                            </label>
                            <input
                                type="date"
                                value={searchParams.get("fromDate") || ""}
                                onChange={(e) => {
                                    const val = e.target.value;
                                    setSearchParams(prev => {
                                        const newP = new URLSearchParams(prev);
                                        if (val) newP.set("fromDate", val);
                                        else newP.delete("fromDate");
                                        newP.set("page", "0");
                                        return newP;
                                    });
                                }}
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                            />
                        </div>

                        {/* Đến ngày */}
                        <div className="min-w-48">
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Đến ngày
                            </label>
                            <input
                                type="date"
                                value={searchParams.get("toDate") || ""}
                                onChange={(e) => {
                                    const val = e.target.value;
                                    setSearchParams(prev => {
                                        const newP = new URLSearchParams(prev);
                                        if (val) newP.set("toDate", val);
                                        else newP.delete("toDate");
                                        newP.set("page", "0");
                                        return newP;
                                    });
                                }}
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                            />
                        </div>

                        {/* Bộ lọc trạng thái */}
                        <div className="min-w-48">
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Trạng thái
                            </label>
                            <select
                                value={statusFilter}
                                onChange={handleStatusChange}
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg bg-white focus:outline-none focus:ring-2 focus:ring-blue-500"
                            >
                                <option value="">Tất cả trạng thái</option>
                                <option value="PENDING">Chờ xác nhận</option>
                                <option value="PAID">Đã thanh toán</option>
                                <option value="CONFIRMED">Đã xác nhận</option>
                                <option value="SHIPPED">Đang giao</option>
                                <option value="DELIVERED">Đã giao</option>
                                <option value="CANCELLED">Đã hủy</option>
                            </select>
                        </div>
                    </div>
                    {/* Tổng doanh thu theo bộ lọc - Card nổi bật */}
                    <div className="mb-8 bg-gradient-to-r from-blue-500 to-blue-600 rounded-xl shadow-lg p-3 text-white">
                        <div className="flex items-center justify-between">
                            <div>
                                <p className="text-blue-100 text-sm font-medium">Tổng doanh thu</p>
                                <p className="text-xl font-bold mt-2">
                                    {Number(totalAmount || 0).toLocaleString("vi-VN")} VNĐ
                                </p>
                                {/* <p className="text-blue-100 text-sm mt-2">
                                    {statusFilter
                                        ? `Theo trạng thái: ${statusLabels[statusFilter] || statusFilter}`
                                        : "Tất cả đơn hàng"
                                    }
                                    {fromDate || toDate ? " • " : ""}
                                    {fromDate && toDate
                                        ? `Từ ${new Date(fromDate).toLocaleDateString("vi-VN")} đến ${new Date(toDate).toLocaleDateString("vi-VN")}`
                                        : fromDate
                                            ? `Từ ${new Date(fromDate).toLocaleDateString("vi-VN")}`
                                            : toDate
                                                ? `Đến ${new Date(toDate).toLocaleDateString("vi-VN")}`
                                                : ""
                                    }
                                </p> */}
                            </div>
                            <div className="text-5xl opacity-30">
                                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                                </svg>
                            </div>
                        </div>
                    </div>
                    {loading ? (
                        <p className="text-center py-10">Đang tải...</p>
                    ) : orders.length === 0 ? (
                        <p className="text-center py-10 text-gray-500">Không có đơn hàng nào.</p>
                    ) : (
                        <>
                            <div className="grid gap-4">
                                {orders
                                    .filter(order => order.status !== "PENDING_PAYMENT")
                                    .map((order) => (
                                        <div key={order.orderId} className="bg-white rounded-lg shadow p-5 hover:shadow-md transition">
                                            <div className="flex justify-between items-start mb-3">
                                                <div>
                                                    <h3 className="font-bold text-lg">#{order.code}</h3>
                                                    <p className="text-sm text-gray-600">
                                                        {new Date(order.orderDate).toLocaleString("vi-VN")}
                                                    </p>
                                                </div>
                                                <span className={`px-3 py-1 rounded-full text-xs font-medium ${statusColors[order.status] || "bg-gray-100"}`}>
                                                    {statusLabels[order.status] || order.status}
                                                </span>
                                            </div>

                                            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
                                                <div><strong>Khách:</strong> {order.customerName}</div>
                                                <div><strong>SĐT:</strong> {order.phoneNumber}</div>
                                                <div><strong>Email:</strong> {order.customerEmail}</div>
                                                <div><strong>Tổng:</strong> {Number(order.finalAmount).toLocaleString()}đ</div>

                                                <div className="md:col-span-4 mt-2 pt-2 border-t border-gray-200">
                                                    <div className="flex items-start gap-2 text-sm">
                                                        <svg className="w-5 h-5 text-gray-500 mt-0.5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                                                        </svg>
                                                        <div className="flex-1">
                                                            <span className="font-medium text-gray-700">Giao đến:</span>{" "}
                                                            <span className="text-gray-600">
                                                                {order.shippingAddress?.length > 80
                                                                    ? `${order.shippingAddress.substring(0, 80)}...`
                                                                    : order.shippingAddress}
                                                            </span>
                                                            {order.shippingAddress?.length > 80 && (
                                                                <button
                                                                    onClick={() => alert(order.shippingAddress)}
                                                                    className="ml-2 text-blue-600 hover:underline text-xs"
                                                                >
                                                                    Xem đầy đủ
                                                                </button>
                                                            )}
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>

                                            <div className="mt-4 gap-2 flex justify-end">
                                                <ViewOrderDetailButton orderId={order.orderId} />
                                                {order.status !== "CANCELLED" && (
                                                    <HandleDelivered orderId={order.orderId} onSuccess={fetchAllOrders} />
                                                )}
                                            </div>
                                        </div>
                                    ))}
                            </div>

                            <span className="text-sm text-gray-600 self-center">
                                Tổng: <strong>{orders.length}</strong> đơn trong trang này
                            </span>
                            <div className="mt-10">
                                <AdminOrderPagination totalPages={totalPages} />
                            </div>
                        </>
                    )}
                </div>
            </main>
        </div>
    );
};

export default AllOrdersAdmin;
