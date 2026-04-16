import React, { useEffect, useState } from "react";
import axios from "axios";
import { useSearchParams } from "react-router-dom";
import PaginationRounded from "../../PaginationRounded";
import api from "../../../api/api";

const Delivery = () => {
    const [orders, setOrders] = useState([]);
    const [totalPages, setTotalPages] = useState(0);
    const [error, setError] = useState("");
    const [searchParams] = useSearchParams();

    const currentPage = Number(searchParams.get("page")) || 1;

    const pageSize = 5;
    const sortBy = "orderedDate";
    const sortOrder = "asc";

    const fetchOrders = async () => {
        try {
            const auth = JSON.parse(localStorage.getItem("auth"));
            const token = auth?.jwtToken;

            if (!token) {
                setError("Bạn chưa đăng nhập hoặc không có quyền.");
                return;
            }

            const response = await api.get("/deliver/orders", {
                params: {
                    pageNumber: currentPage - 1,
                    pageSize,
                    sortBy,
                    sortOrder
                },
                headers: {
                    Authorization: `Bearer ${token}`
                }
            });

            setOrders(response.data.content);
            setTotalPages(response.data.totalPages);
            setError("");
        } catch (err) {
            if (err.response?.status === 401) {
                setError("Bạn không có quyền truy cập. Vui lòng đăng nhập lại với tài khoản DELIVER.");
            } else {
                setError("Đã xảy ra lỗi khi tải đơn hàng.");
            }
            setOrders([]);
        }
    };

    useEffect(() => {
        fetchOrders();
    }, [currentPage]);

    return (
        <div className="p-6">
            <h1 className="text-2xl font-semibold mb-4">Đơn hàng đang giao</h1>

            {error && <p className="text-red-500 mb-4">{error}</p>}

            <table className="min-w-full border border-gray-300">
                <thead className="bg-gray-100">
                    <tr>
                        <th className="p-2 border">Mã đơn</th>
                        <th className="p-2 border">Email</th>
                        <th className="p-2 border">Tên người dùng</th>
                        <th className="p-2 border">Thanh toán</th>
                        <th className="p-2 border">Trạng thái giao</th>
                    </tr>
                </thead>
                <tbody>
                    {orders.length > 0 ? (
                        orders.map((order) => (
                            <tr key={order.orderId} className="hover:bg-gray-50">
                                <td className="p-2 border">{order.orderId}</td>
                                <td className="p-2 border">{order.email}</td>
                                <td className="p-2 border">{order.userName}</td>
                                <td className="p-2 border">{order.paymentStatus}</td>
                                <td className="p-2 border">{order.deliveryStatus}</td>
                            </tr>
                        ))
                    ) : (
                        !error && (
                            <tr>
                                <td colSpan="5" className="text-center p-4 text-gray-500">
                                    Không có đơn hàng nào.
                                </td>
                            </tr>
                        )
                    )}
                </tbody>
            </table>

            {totalPages > 1 && <PaginationRounded numberofPage={totalPages} />}
        </div>
    );
};

export default Delivery;
