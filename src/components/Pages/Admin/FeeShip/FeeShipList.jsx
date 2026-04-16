// src/admin/shipping/FeeShipList.jsx
import React, { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";

import { FiEdit3 } from "react-icons/fi";

import AdminSidebar from "../AdminSidebar";
import PaginationRounded from "../../../PaginationRounded";
import api from "../../../../api/api";
import toast from "react-hot-toast";

const FeeShipList = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    const [fees, setFees] = useState([]);
    const [pagination, setPagination] = useState({
        pageNumber: 0,
        pageSize: 15,
        totalElements: 0,
        totalPages: 1,
        first: true,
        last: false,
    });
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState("");

    const page = Number(searchParams.get("page") || 1); // trang người dùng thấy (bắt đầu từ 1)
    const getToken = () => JSON.parse(localStorage.getItem("auth"))?.jwtToken;
    const fetchFees = async () => {
        try {
            setLoading(true);
            const pageIndex = page - 1;
            const search = searchTerm.trim() ? `&search=${encodeURIComponent(searchTerm)}` : "";
            const token = getToken();
            const res = await api.get(
                `/shipping/fee/all?page=${pageIndex}&size=15${search}`,

                {
                    headers: {
                        'Authorization': token ? `Bearer ${token}` : '',
                    }
                }
            );


            setFees(res.data.content || []);
            setPagination({
                pageNumber: res.data.pageable.pageNumber,
                pageSize: res.data.pageSize,
                totalElements: res.data.totalElements,
                totalPages: res.data.totalPages,
                first: res.data.first,
                last: res.data.last,
            });
        } catch (err) {
            console.error("Lỗi tải phí ship:", err);
            toast.error("Không tải được danh sách phí vận chuyển");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchFees();
    }, [page, searchTerm]);

    const handleSearch = (e) => {
        e.preventDefault();
        if (page !== 1) {
            setSearchParams({ page: 1 });
        } else {
            fetchFees();
        }
    };

    const formatMoney = (value) => {
        if (value == null) return "—";
        return Number(value).toLocaleString("vi-VN") + " ₫";
    };

    return (
        <div className="flex min-h-screen bg-gray-100">
            <AdminSidebar />

            <div className="flex-1 p-6 mt-[50px]">
                <div className="flex items-center justify-between mb-6">
                    <h1 className="text-3xl font-bold text-slate-800">Quản lý phí vận chuyển theo tỉnh</h1>
                </div>

                {/* Tìm kiếm */}
                <div className="mb-5 bg-white p-4 rounded-lg shadow">
                    <form onSubmit={handleSearch} className="flex gap-3">
                        <input
                            type="text"
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            placeholder="Tìm tỉnh/thành phố..."
                            className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                        />
                        <button
                            type="submit"
                            className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition font-medium"
                        >
                            Tìm kiếm
                        </button>
                        {searchTerm && (
                            <button
                                type="button"
                                onClick={() => {
                                    setSearchTerm("");
                                    if (page !== 1) setSearchParams({ page: 1 });
                                }}
                                className="px-4 py-2 bg-gray-500 text-white rounded-lg hover:bg-gray-600 transition"
                            >
                                Xóa bộ lọc
                            </button>
                        )}
                    </form>
                </div>

                {/* Bảng */}
                <div className="bg-white rounded-lg shadow overflow-hidden">
                    {loading ? (
                        <div className="p-12 text-center text-gray-500">Đang tải dữ liệu...</div>
                    ) : (
                        <table className="min-w-full divide-y divide-gray-200">
                            <thead className="bg-gray-50">
                                <tr>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        ID Tỉnh
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Tỉnh/Thành phố
                                    </th>
                                    <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Phí ship mặc định
                                    </th>
                                    <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Miễn phí ship từ
                                    </th>
                                    <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Thao tác
                                    </th>
                                </tr>
                            </thead>
                            <tbody className="bg-white divide-y divide-gray-200">
                                {fees.length > 0 ? (
                                    fees.map((item) => (
                                        <tr key={item.provinceId} className="hover:bg-gray-50 transition">
                                            <td className="px-6 py-4 text-sm text-gray-600 font-mono">
                                                {item.provinceId}
                                            </td>
                                            <td className="px-6 py-4 text-sm font-medium text-gray-900">
                                                {item.provinceName}
                                            </td>
                                            <td className="px-6 py-4 text-center text-sm">
                                                <span className="font-semibold text-blue-600">
                                                    {formatMoney(item.defaultFee)}
                                                </span>
                                            </td>
                                            <td className="px-6 py-4 text-center text-sm">
                                                <span className="font-medium text-green-600">
                                                    {formatMoney(item.freeShippingThreshold)}
                                                </span>
                                            </td>
                                            <td className="px-6 py-4 text-center">
                                                <Link
                                                    to={`/admin/shipping/edit/${item.provinceId}`}
                                                    className="inline-flex items-center gap-1 px-4 py-2 bg-amber-500 hover:bg-amber-600 text-white rounded-md transition text-sm font-medium"
                                                >
                                                    <FiEdit3 size={16} />
                                                    Sửa phí
                                                </Link>
                                            </td>
                                        </tr>
                                    ))
                                ) : (
                                    <tr>
                                        <td colSpan="5" className="px-6 py-12 text-center text-gray-500 text-lg">
                                            Không tìm thấy tỉnh nào.
                                        </td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                    )}
                </div>

                {/* Phân trang */}
                <div className="mt-6 flex flex-col items-center">
                    <p className="text-sm text-gray-600 mb-3">
                        Trang <strong>{page}</strong> / {pagination.totalPages} — Tổng:{" "}
                        <strong>{pagination.totalElements}</strong> tỉnh
                    </p>
                    <PaginationRounded numberofPage={pagination.totalPages || 1} />
                </div>
            </div>
        </div>
    );
};

export default FeeShipList;