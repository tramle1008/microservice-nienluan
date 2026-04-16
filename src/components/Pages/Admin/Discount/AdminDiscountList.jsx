// src/pages/admin/discount/AdminDiscountList.jsx
import React, { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import AdminSidebar from "../AdminSidebar";
import { toast } from "react-hot-toast";
import api from "../../../../api/api";

import {
    Dialog, DialogTitle, DialogContent,
    DialogContentText, DialogActions, Button
} from "@mui/material";
import PaginationRounded from "../../../PaginationRounded";

const AdminDiscountList = () => {
    const [discounts, setDiscounts] = useState([]);
    const [totalPages, setTotalPages] = useState(1);
    const [loading, setLoading] = useState(true);
    const [openDelete, setOpenDelete] = useState(false);
    const [selectedId, setSelectedId] = useState(null);

    const [searchParams, setSearchParams] = useSearchParams();

    // Lấy tất cả param từ URL
    const pageNumber = Number(searchParams.get("pageNumber")) || 0;
    const pageSize = Number(searchParams.get("pageSize")) || 10;
    const sortBy = searchParams.get("sortBy") || "discountId";
    const sortOrder = searchParams.get("sortOrder") || "desc";
    const keyword = searchParams.get("keyword") || "";
    const activeFilter = searchParams.get("active"); // "true" | "false" | null

    useEffect(() => {
        fetchDiscounts();
    }, [searchParams]); // ← reload khi bất kỳ param nào thay đổi

    const fetchDiscounts = async () => {
        setLoading(true);
        try {
            const token = JSON.parse(localStorage.getItem("auth"))?.jwtToken;

            // Tạo query string động
            const params = new URLSearchParams();
            params.append("pageNumber", pageNumber);
            params.append("pageSize", pageSize);
            params.append("sortBy", sortBy);
            params.append("sortOrder", sortOrder);

            if (keyword) params.append("keyword", keyword);
            if (activeFilter !== null) params.append("active", activeFilter);

            const res = await api.get(`/discounts/public?${params.toString()}`, {
                headers: { Authorization: `Bearer ${token}` }
            });

            const data = res.data;
            setDiscounts(data.content || []);
            setTotalPages(data.totalPages || 1);

        } catch (err) {
            console.error(err);
            toast.error("Lỗi tải danh sách khuyến mãi");
            setDiscounts([]);
            setTotalPages(1);
        } finally {
            setLoading(false);
        }
    };

    // Hàm thay đổi filter active
    const handleFilterActive = (value) => {
        if (value === "all") {
            searchParams.delete("active");
        } else {
            searchParams.set("active", value);
        }
        searchParams.set("pageNumber", "0"); // reset về trang đầu
        setSearchParams(searchParams);
    };

    const deleteDiscount = async () => {
        try {
            const token = JSON.parse(localStorage.getItem("auth"))?.jwtToken;
            await api.delete(`/discounts/${selectedId}`, {
                headers: { Authorization: `Bearer ${token}` }
            });
            toast.success("Xóa thành công!");
            setOpenDelete(false);
            fetchDiscounts();
        } catch (err) {
            toast.error("Xóa thất bại!");
        }
    };

    const formatDate = (date) => new Date(date).toLocaleString("vi-VN");
    const formatMoney = (amount) => amount ? amount.toLocaleString("vi-VN") + "₫" : "Không giới hạn";

    const getStatusBadge = (d) => {
        const now = new Date();
        const start = new Date(d.startDate);
        const end = new Date(d.endDate);

        if (!d.active) return <span className="px-3 py-1 bg-gray-500 text-white rounded-full text-xs font-medium">Tắt</span>;
        if (now < start) return <span className="px-3 py-1 bg-blue-500 text-white rounded-full text-xs font-medium">Sắp tới</span>;
        if (now > end) return <span className="px-3 py-1 bg-red-500 text-white rounded-full text-xs font-medium">Hết hạn</span>;
        return <span className="px-3 py-1 bg-green-500 text-white rounded-full text-xs font-bold">Đang chạy</span>;
    };

    if (loading) return <div className="p-8 text-center text-xl">Đang tải...</div>;

    return (
        <div className="flex min-h-screen bg-gray-100">
            <AdminSidebar />
            <div className="flex-1 p-8 mt-[50px]">
                <div className="flex justify-between items-center mb-8">
                    <h1 className="text-3xl font-bold text-slate-800">Quản lý Khuyến Mãi</h1>
                    <Link to="/admin/discounts/add"
                        className="bg-gradient-to-r bg-emerald-700 hover:from-red-300 hover:to-pink-300 text-white px-6 py-3 rounded-lg font-bold shadow-lg transition">
                        + Tạo khuyến mãi mới
                    </Link>
                </div>

                {/* FILTER ACTIVE */}
                <div className="mb-6 flex gap-3">
                    <button onClick={() => handleFilterActive("all")}
                        className={`px-5 py-2 rounded-lg font-medium transition ${activeFilter === null ? "bg-blue-600 text-white" : "bg-gray-200"}`}>
                        Tất cả
                    </button>
                    <button onClick={() => handleFilterActive("true")}
                        className={`px-5 py-2 rounded-lg font-medium transition ${activeFilter === "true" ? "bg-green-600 text-white" : "bg-gray-200"}`}>
                        Đang hoạt động
                    </button>
                    <button onClick={() => handleFilterActive("false")}
                        className={`px-5 py-2 rounded-lg font-medium transition ${activeFilter === "false" ? "bg-red-600 text-white" : "bg-gray-200"}`}>
                        Đã tắt
                    </button>
                </div>

                <div className="bg-white rounded-xl shadow-lg overflow-hidden">
                    <table className="min-w-full">
                        {/* ... table header như cũ ... */}
                        <thead className="bg-gradient-to-r from-gray-800 to-gray-900 text-white">
                            <tr>
                                <th className="px-6 py-4 text-left text-xs font-bold uppercase">ID</th>
                                <th className="px-6 py-4 text-left text-xs font-bold uppercase">Tên khuyến mãi</th>
                                <th className="px-6 py-4 text-center text-xs font-bold uppercase">Giảm</th>
                                <th className="px-6 py-4 text-center text-xs font-bold uppercase">Max giảm</th>
                                <th className="px-6 py-4 text-center text-xs font-bold uppercase">Thời gian</th>
                                <th className="px-6 py-4 text-center text-xs font-bold uppercase">Áp dụng cho</th>
                                <th className="px-6 py-4 text-center text-xs font-bold uppercase">Trạng thái</th>
                                <th className="px-6 py-4 text-center text-xs font-bold uppercase">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-200">
                            {discounts.length === 0 ? (
                                <tr><td colSpan="8" className="text-center py-16 text-gray-500 text-lg">Không có khuyến mãi nào</td></tr>
                            ) : (
                                discounts.map((d) => (
                                    <tr key={d.discountId} className="hover:bg-gray-50 transition">
                                        <td className="px-6 py-4 font-medium">#{d.discountId}</td>
                                        <td className="px-6 py-4 font-semibold text-gray-800">{d.name}</td>
                                        <td className="px-6 py-4 text-center text-red-600 font-bold text-lg">
                                            {d.type === "PERCENTAGE" ? `-${d.percentage}%` : `- ${formatMoney(d.percentage)}`}
                                        </td>
                                        <td className="px-6 py-4 text-center text-gray-600">{formatMoney(d.maxAmount)}</td>
                                        <td className="px-6 py-4 text-center text-xs">
                                            <div>Từ: {formatDate(d.startDate)}</div>
                                            <div>Đến: {formatDate(d.endDate)}</div>
                                        </td>
                                        <td className="px-6 py-4 text-center">
                                            <span className="px-3 py-1 bg-purple-100 text-purple-700 rounded-full text-xs font-medium">
                                                {d.target === "PRODUCT" ? "Sản phẩm" : "Biến thể"}
                                            </span>
                                        </td>
                                        <td className="px-6 py-4 text-center">{getStatusBadge(d)}</td>
                                        <td className="px-6 py-4 text-center space-x-4">
                                            <Link to={`/admin/discount/edit/${d.discountId}`} className="text-indigo-600 hover:text-indigo-800 font-medium">Sửa</Link>
                                            <button onClick={() => { setSelectedId(d.discountId); setOpenDelete(true); }}
                                                className="text-red-600 hover:text-red-800 font-medium">Xóa</button>
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>

                {/* PHÂN TRANG */}
                <div className="mt-8 flex justify-center">
                    <PaginationRounded numberofPage={totalPages} />
                </div>
            </div>

            {/* Dialog Xóa */}
            <Dialog open={openDelete} onClose={() => setOpenDelete(false)}>
                <DialogTitle className="font-bold text-red-600">Xóa khuyến mãi?</DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        Hành động này <strong>không thể hoàn tác</strong>.<br />
                        Bạn có chắc chắn muốn xóa không?
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setOpenDelete(false)}>Hủy</Button>
                    <Button onClick={deleteDiscount} color="error" variant="contained">Xóa</Button>
                </DialogActions>
            </Dialog>
        </div>
    );
};

export default AdminDiscountList;