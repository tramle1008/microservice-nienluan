import { useEffect, useState } from "react";
import axios from "axios";
import AdminSidebar from "./AdminSidebar";
import StatCard from "./StatCard";
import { Link } from "react-router-dom";
import { useSearchParams } from "react-router-dom";
import ReusableFilter from "../../ReusableFilter";
import api from "../../../api/api";

const DetailOrder = () => {
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const [searchParams] = useSearchParams();

    useEffect(() => {
        const fetchOrders = async () => {
            try {
                const token = JSON.parse(localStorage.getItem("auth"))?.jwtToken;

                const status = searchParams.get("status");  // filter
                const sortOrder = searchParams.get("sortOrder") || "asc"; // sort
                const key = searchParams.get("key") || ""; // search

                const params = new URLSearchParams();
                if (status) params.set("status", status);
                if (key) params.set("key", key);
                if (sortOrder) params.set("sortOrder", sortOrder);
                params.set("pageNumber", 0);
                params.set("pageSize", 10);

                const res = await api.get(`/admin/orders?${params.toString()}`, {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                });

                setOrders(res.data?.content || []);
            } catch (err) {
                setError("Không thể tải đơn hàng. Vui lòng kiểm tra quyền truy cập.");
                console.error(err);
            } finally {
                setLoading(false);
            }
        };

        fetchOrders();
    }, [searchParams]);

    const getStatusStyle = (status) => {
        switch (status) {
            case "PENDING":
                return "bg-amber-200 text-amber-800";
            case "SHIPPED":
                return "bg-blue-200 text-blue-800";
            case "DELIVERED":
                return "bg-green-500 text-white";
            case "REJECTED":
                return "bg-red-200 text-red-700";
            default:
                return "text-gray-500";
        }
    };

    if (loading) return <p className="text-center mt-6 text-gray-600">Đang tải dữ liệu...</p>;
    if (error) return <p className="text-red-500 text-center mt-6">{error}</p>;

    return (
        <div className="flex flex-col lg:flex-row min-h-screen">
            <AdminSidebar />

            <main className="flex-1 px-6 py-8 mt-18">
                <ReusableFilter
                    filterLabel="Trạng thái đơn"
                    filterParam="status"
                    filterList={[
                        { value: "PENDING", label: "PENDING" },
                        { value: "SHIPPED", label: "SHIPPED" },
                        { value: "DELIVERED", label: "DELIVERED" },
                        { value: "REJECTED", label: "REJECTED" },
                    ]}
                />
                <div className="overflow-x-auto mt-2">

                    <table className="min-w-full border border-gray-300 rounded-md overflow-hidden">
                        <thead className="bg-gray-100 text-gray-700 text-sm">
                            <tr>
                                <th className="px-4 py-2 border-b text-left">Mã đơn</th>
                                <th className="px-4 py-2 border-b text-left">Họ tên</th>
                                <th className="px-4 py-2 border-b text-left">Trạng thái</th>
                                <th className="px-4 py-2 border-b text-left">Thanh toán</th>
                                <th className="px-4 py-2 border-b text-left">Địa chỉ</th>
                                <th className="px-4 py-2 border-b text-left">Số tiền (VNĐ)</th>
                                <th className="px-4 py-2 border-b text-left">Ngày đặt</th>
                            </tr>
                        </thead>
                        <tbody>
                            {orders.length > 0 ? (
                                orders.map((order) => (
                                    <tr key={order.orderId} className="hover:bg-gray-50 text-sm">
                                        <td className="px-4 py-2 border-b">{order.orderId}</td>
                                        <td className="px-4 py-2 border-b">{order.userName || "Không rõ"}</td>
                                        <td className={`px-4 py-2 border-b font-semibold ${getStatusStyle(order.deliveryStatus)}`}>
                                            {order.deliveryStatus}
                                        </td>
                                        <td className="px-4 py-2 border-b">{order.paymentStatus}</td>
                                        <td className="px-4 py-2 border-b">
                                            {order.address?.detail ? `${order.address.detail}, ` : ""}
                                            {order.address?.ward}, {order.address?.district}, {order.address?.province}
                                        </td>
                                        <td className="px-4 py-2 border-b">{Number(order.totalAmount).toLocaleString()}</td>
                                        <td className="px-4 py-2 border-b">{order.dateOrder}</td>
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td colSpan={7} className="text-center text-gray-500 py-4">
                                        Không có đơn hàng nào.
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>

            </main>
        </div>
    );
};

export default DetailOrder;
