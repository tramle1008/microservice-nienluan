// src/pages/admin/discount/AdminDiscountAdd.jsx
import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import AdminSidebar from "../AdminSidebar";
import axios from "axios";
import { toast } from "react-hot-toast";

const AdminDiscountAdd = () => {
    const navigate = useNavigate();

    const [form, setForm] = useState({
        name: "",
        percentage: "",
        maxAmount: "",
        startDate: "",
        endDate: "",
        active: true,
        target: "PRODUCT",
        type: "PERCENTAGE"
    });

    const [errors, setErrors] = useState({});

    // Lấy token (bắt buộc)
    const getToken = () => {
        return JSON.parse(localStorage.getItem("auth"))?.jwtToken;
    };

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        setForm(prev => ({
            ...prev,
            [name]: type === "checkbox" ? checked : value
        }));
        // Xóa lỗi khi người dùng sửa
        if (errors[name]) {
            setErrors(prev => ({ ...prev, [name]: null }));
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrors({});

        const token = getToken();
        if (!token) {
            toast.error("Bạn chưa đăng nhập!");
            return;
        }

        try {
            const payload = {
                ...form,
                percentage: parseFloat(form.percentage) || 0,
                maxAmount: form.maxAmount ? parseFloat(form.maxAmount) : null,
                startDate: form.startDate ? `${form.startDate}:00` : null,
                endDate: form.endDate ? `${form.endDate}:00` : null,
            };

            console.log("Gửi lên:", payload); // để bạn kiểm tra

            await axios.post(
                "/api/discounts/create", // endpoint của bạn
                payload,
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                        "Content-Type": "application/json"
                    }
                }
            );

            toast.success("Tạo khuyến mãi thành công!", {
                icon: "Success",
                duration: 4000,
                style: {
                    background: "#10b981",
                    color: "white",
                    fontWeight: "bold"
                }
            });

            // Chuyển về danh sách sau 1.5s để thấy toast
            setTimeout(() => navigate("/admin/discounts"), 1500);

        } catch (err) {
            console.error("Lỗi từ server:", err.response?.data);

            // Trường 1: Backend trả lỗi validation dạng mảng (Spring @Valid)
            if (err.response?.data?.errors && Array.isArray(err.response.data.errors)) {
                const serverErrors = {};
                err.response.data.errors.forEach(error => {
                    serverErrors[error.field] = error.defaultMessage;
                });
                setErrors(serverErrors);

                const msg = Object.values(serverErrors).join(" • ");
                toast.error(msg, { duration: 8000 });
            }
            // Trường 2: Backend trả message + field
            else if (err.response?.data?.message) {
                toast.error(err.response.data.message);

                if (err.response.data.field) {
                    setErrors({ [err.response.data.field]: err.response.data.message });
                }
            }
            // Trường 3: Lỗi khác
            else {
                toast.error("Có lỗi xảy ra, vui lòng thử lại!");
            }
        }
    };

    return (
        <div className="flex min-h-screen bg-gray-100">
            <AdminSidebar />
            <div className="flex-1 p-8 mt-[50px]">
                <div className="max-w-2xl mx-auto">
                    <div className="flex items-center mb-8">
                        <Link to="/admin/discounts" className="text-gray-600 hover:text-gray-800 mr-4 px-6 py-3 bg-gray-200 hover:bg-gray-300 rounded-lg transition">
                            ← Quay lại
                        </Link>
                        <h1 className="text-3xl font-bold text-slate-800">Tạo khuyến mãi mới</h1>
                    </div>

                    <form onSubmit={handleSubmit} className="bg-white rounded-2xl shadow-xl p-10 space-y-7">
                        {/* Các input giống hệt bạn, chỉ thêm lỗi đỏ */}
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">Tên khuyến mãi *</label>
                            <input type="text" name="name" required value={form.name} onChange={handleChange}
                                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:outline-none"
                                placeholder="VD: Sale Noel 70%" />
                            {errors.name && <p className="text-red-500 text-xs mt-1">{errors.name}</p>}
                        </div>

                        <div className="grid grid-cols-2 gap-6">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">Loại giảm giá</label>
                                <select name="type" value={form.type} onChange={handleChange}
                                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500">
                                    <option value="PERCENTAGE">Giảm theo %</option>
                                    <option value="FIXED_AMOUNT">Giảm cố định (₫)</option>
                                </select>
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">Giá trị giảm *</label>
                                <input type="number" name="percentage" required value={form.percentage} onChange={handleChange}
                                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500"
                                    placeholder={form.type === "PERCENTAGE" ? "30" : "500000"} />
                                {errors.percentage && <p className="text-red-500 text-xs mt-1">{errors.percentage}</p>}
                            </div>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">Giới hạn giảm tối đa (₫)</label>
                            <input type="number" name="maxAmount" value={form.maxAmount} onChange={handleChange}
                                className="w-full px-4 py-3 border border-gray-300 rounded-lg"
                                placeholder="Không bắt buộc, ví dụ: 1000000" />
                        </div>

                        <div className="grid grid-cols-2 gap-6">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">Ngày bắt đầu *</label>
                                <input type="datetime-local" name="startDate" required value={form.startDate} onChange={handleChange}
                                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500" />
                                {errors.startDate && <p className="text-red-500 text-xs mt-1">{errors.startDate}</p>}
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">Ngày kết thúc *</label>
                                <input type="datetime-local" name="endDate" required value={form.endDate} onChange={handleChange}
                                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500" />
                                {errors.endDate && <p className="text-red-500 text-xs mt-1">{errors.endDate}</p>}
                            </div>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">Áp dụng cho</label>
                            <select name="target" value={form.target} onChange={handleChange}
                                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500">
                                <option value="PRODUCT">Sản phẩm</option>
                                <option value="VARIANT">Biến thể</option>
                            </select>
                        </div>

                        <div className="flex items-center">
                            <input type="checkbox" name="active" checked={form.active} onChange={handleChange}
                                className="w-5 h-5 text-red-600 rounded focus:ring-red-500" />
                            <label className="ml-3 text-sm font-medium text-gray-700">Kích hoạt ngay</label>
                        </div>

                        <div className="pt-8 flex gap-4">
                            <button type="submit"
                                className="bg-gradient-to-r from-red-600 to-pink-600 text-white px-10 py-4 rounded-lg font-bold text-lg shadow-lg hover:shadow-2xl transform hover:scale-105 transition">
                                Tạo khuyến mãi
                            </button>
                            <Link to="/admin/discounts"
                                className="px-10 py-4 bg-gray-300 text-gray-700 rounded-lg font-medium hover:bg-gray-400 transition">
                                Hủy
                            </Link>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default AdminDiscountAdd;