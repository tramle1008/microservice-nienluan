import { useEffect, useState } from "react";
import axios from "axios";
import { useSearchParams } from "react-router-dom";
import PaginationRounded from "../../PaginationRounded";
import AdminSidebar from "./AdminSidebar";

import HandleReject from "../Order_Admin/HandleReject";
import api from "../../../api/api";
import AdminOrderPagination from "./AdminOrderPagination";
import HandleConfirm from "../Order_Admin/HandleConfirm";
import ViewOrderDetailButton from "./ViewOrderAdmin/ViewOrderDetailButton";

const HandleOrder = () => {
    const [orders, setOrders] = useState([]);
    const [totalPages, setTotalPages] = useState(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    // Lấy các param từ URL
    const [searchParams] = useSearchParams();

    // Đọc param từ URL, nếu không có thì dùng giá trị mặc định
    const page = parseInt(searchParams.get("page") ?? "0", 10);        // backend dùng page=0
    const size = parseInt(searchParams.get("size") ?? "10", 10);
    const sortBy = searchParams.get("sortBy") ?? "orderDate";
    const sortDir = searchParams.get("sortDir") ?? "desc";

    const fetchPendingOrders = async () => {
        setLoading(true);
        setError("");

        try {
            const token = JSON.parse(localStorage.getItem("auth"))?.jwtToken;

            const response = await api.get("/orders/pending", {
                params: {
                    page,      // backend nhận: page=0,1,2...
                    size,      // số phần tử mỗi trang
                    sortBy,    // tên field: orderDate, totalAmount, ...
                    sortDir    // asc hoặc desc
                },
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });

            setOrders(response.data.content);
            setTotalPages(response.data.totalPages);
        } catch (err) {
            console.error("Lỗi tải đơn hàng:", err);
            setError("Không thể tải đơn hàng. Vui lòng kiểm tra lại token hoặc mạng.");
        } finally {
            setLoading(false);
        }
    };

    // Gọi lại khi bất kỳ param nào thay đổi
    useEffect(() => {
        fetchPendingOrders();
    }, [page, size, sortBy, sortDir]);

    if (loading) return <p className="text-center mt-10 text-gray-600">Đang tải đơn hàng...</p>;
    if (error) return <p className="text-center mt-10 text-red-500">{error}</p>;

    return (
        <div className="flex min-h-screen bg-gray-100">
            <AdminSidebar />

            <main className="flex-1 p-6">
                <section className="max-w-6xl mx-auto">
                    <h2 className="text-3xl font-bold text-center text-gray-800 mb-8">
                        Đơn hàng chờ xác nhận
                    </h2>

                    {orders.length === 0 ? (
                        <p className="text-center text-gray-500 text-lg">Không có đơn hàng nào đang chờ.</p>
                    ) : (
                        <>
                            <div className="grid gap-6">
                                {orders.map((order) => (
                                    <div
                                        key={order.orderId}
                                        className="bg-white rounded-xl shadow-md hover:shadow-xl transition-shadow p-6 border border-gray-200"
                                    >
                                        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm mb-4">
                                            <div><strong>Mã đơn:</strong> #{order.code || order.orderId}</div>
                                            <div><strong>Khách hàng:</strong> {order.customerName}</div>
                                            <div><strong>Email:</strong> {order.customerEmail}</div>
                                            <div><strong>SĐT:</strong> {order.phoneNumber}</div>
                                            <div><strong>Ngày đặt:</strong> {new Date(order.orderDate).toLocaleString("vi-VN")}</div>
                                            <div><strong>Tổng tiền:</strong> {Number(order.totalAmount).toLocaleString()} đ</div>
                                        </div>

                                        <div className="text-sm text-gray-600 mb-3">
                                            <strong>Địa chỉ giao:</strong> {order.shippingAddress}
                                        </div>

                                        <div className="flex gap-3 mt-5 justify-end">
                                            <ViewOrderDetailButton orderId={order.orderId} />
                                            <HandleConfirm orderId={order.orderId} onSuccess={fetchPendingOrders} />
                                            <HandleReject orderId={order.orderId} onSuccess={fetchPendingOrders} />

                                        </div>
                                    </div>
                                ))}
                            </div>

                            {/* Phân trang */}
                            <div className="mt-10 flex justify-center">
                                <AdminOrderPagination totalPages={totalPages} />
                            </div>
                        </>
                    )}
                </section>
            </main>
        </div>
    );
};

export default HandleOrder;