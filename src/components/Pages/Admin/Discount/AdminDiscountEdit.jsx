
import React, { useState, useEffect } from "react";
import { Link, useParams, useNavigate } from "react-router-dom";
import AdminSidebar from "../AdminSidebar";
import axios from "axios";
import { toast } from "react-hot-toast";
import api from "../../../../api/api";

const AdminDiscountEdit = () => {
    const { id } = useParams();
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
    const [loading, setLoading] = useState(true);
    const [errors, setErrors] = useState({});

    const getToken = () => JSON.parse(localStorage.getItem("auth"))?.jwtToken;

    useEffect(() => {
        const fetchDiscount = async () => {
            const token = getToken();
            if (!token) {
                toast.error("Bạn chưa đăng nhập!");
                navigate("/login");
                return;
            }

            try {
                setLoading(true);

                // SAI: const res = api.get(...) → res là Promise
                // ĐÚNG: phải có await + .data
                const res = await api.get(`/discounts/${id}`, {
                    headers: { Authorization: `Bearer ${token}` }
                });

                const d = res.data; // BÂY GIỜ MỚI CÓ DATA!

                setForm({
                    name: d.name || "",
                    percentage: d.percentage || "",
                    maxAmount: d.maxAmount || "",
                    startDate: d.startDate ? d.startDate.slice(0, 16) : "",
                    endDate: d.endDate ? d.endDate.slice(0, 16) : "",
                    active: d.active,
                    target: d.target || "PRODUCT",
                    type: d.type || "PERCENTAGE"
                });

            } catch (err) {
                console.error("Lỗi load discount:", err);
                toast.error("Không tải được khuyến mãi! Có thể ID không tồn tại.");
                navigate("/admin/discounts");
            } finally {
                setLoading(false);
            }
        };

        fetchDiscount();
    }, [id, navigate]);

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        setForm(prev => ({
            ...prev,
            [name]: type === "checkbox" ? checked : value
        }));
        if (errors[name]) setErrors(prev => ({ ...prev, [name]: null }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrors({});

        const token = getToken();
        if (!token) {
            toast.error("Token hết hạn!");
            return;
        }

        try {
            const payload = {
                ...form,
                percentage: form.percentage ? parseFloat(form.percentage) : null,
                maxAmount: form.maxAmount ? parseFloat(form.maxAmount) : null,
                startDate: form.startDate ? `${form.startDate}:00` : null,
                endDate: form.endDate ? `${form.endDate}:00` : null,
            };

            await axios.put(`/api/discounts/create/${id}`, payload, {
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json"
                }
            });

            toast.success("Cập nhật khuyến mãi thành công!", {
                icon: "Success",
                style: { background: "#10b981", color: "white", fontWeight: "bold" }
            });

            setTimeout(() => navigate("/admin/discounts"), 1500);

        } catch (err) {
            console.error(err.response?.data);
            if (err.response?.data?.errors && Array.isArray(err.response.data.errors)) {
                const serverErrors = {};
                err.response.data.errors.forEach(error => {
                    serverErrors[error.field] = error.defaultMessage;
                });
                setErrors(serverErrors);
                toast.error(Object.values(serverErrors).join(" • "), { duration: 8000 });
            } else if (err.response?.data?.message) {
                toast.error(err.response.data.message);
            } else {
                toast.error("Cập nhật thất bại!");
            }
        }
    };

    if (loading) return (
        <div className="flex min-h-screen bg-gray-100">
            <AdminSidebar />
            <div className="flex-1 p-8 mt-[50px] flex items-center justify-center">
                <div className="text-2xl text-gray-600">Đang tải khuyến mãi...</div>
            </div>
        </div>
    );

    return (
        <div className="flex min-h-screen bg-gray-100">
            <AdminSidebar />
            <div className="flex-1 p-8 mt-[50px]">
                <div className="max-w-2xl mx-auto">
                    <div className="flex items-center mb-8">
                        <Link to="/admin/discounts" className="text-gray-600 hover:text-gray-800 mr-4 px-6 py-3 bg-gray-200 hover:bg-gray-300 rounded-lg transition">
                            Quay lại
                        </Link>
                        <h1 className="text-3xl font-bold text-slate-800">Chỉnh sửa khuyến mãi</h1>
                    </div>

                    <form onSubmit={handleSubmit} className="bg-white rounded-2xl shadow-xl p-10 space-y-7">
                        <div className="text-center text-lg font-semibold text-gray-700 mb-4">
                            ID: #{id}
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">Tên khuyến mãi</label>
                            <input type="text" name="name" value={form.name} onChange={handleChange}
                                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                                placeholder="Sale Noel 2025" disabled />
                            {errors.name && <p className="text-red-500 text-xs mt-1">{errors.name}</p>}
                        </div>

                        <div className="grid grid-cols-2 gap-6">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">Loại giảm giá</label>
                                <div className="w-full px-4 py-3 border border-gray-300 rounded-lg bg-gray-100 text-gray-700 font-medium">
                                    {form.type === "PERCENTAGE" ? "Giảm theo phần trăm (%)" : "Giảm cố định (số tiền cố định)"}
                                </div>
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">Giá trị giảm</label>
                                <input type="number" name="percentage" value={form.percentage} onChange={handleChange}
                                    className="w-full px-4 py-3 border border-gray-300 rounded-lg"
                                    disabled />
                                {errors.percentage && <p className="text-red-500 text-xs mt-1">{errors.percentage}</p>}
                            </div>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">Giới hạn giảm tối đa (₫)</label>
                            <input type="number" name="maxAmount" value={form.maxAmount} onChange={handleChange}
                                className="w-full px-4 py-3 border border-gray-300 rounded-lg" disabled
                                placeholder="Không bắt buộc" />
                        </div>

                        <div className="grid grid-cols-2 gap-6">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">Ngày bắt đầu</label>
                                <input type="datetime-local" name="startDate" value={form.startDate} onChange={handleChange}
                                    className="w-full px-4 py-3 border border-gray-300 rounded-lg" disabled />
                                {errors.startDate && <p className="text-red-500 text-xs mt-1">{errors.startDate}</p>}
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">Ngày kết thúc</label>
                                <input type="datetime-local" name="endDate" value={form.endDate} onChange={handleChange}
                                    className="w-full px-4 py-3 border border-gray-300 rounded-lg" disabled />
                                {errors.endDate && <p className="text-red-500 text-xs mt-1">{errors.endDate}</p>}
                            </div>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">Áp dụng cho</label>
                            <select name="target" value={form.target} onChange={handleChange} disabled
                                className="w-full px-4 py-3 border border-gray-300 rounded-lg">
                                <option value="PRODUCT">Sản phẩm</option>
                                <option value="VARIANT">Biến thể</option>
                            </select>
                        </div>

                        <div className="flex items-center">
                            <input type="checkbox" name="active" checked={form.active} onChange={handleChange}
                                className="w-5 h-5 text-green-600 rounded focus:ring-green-500" />
                            <label className="ml-3 text-sm font-medium text-gray-700">Kích hoạt khuyến mãi</label>
                        </div>

                        <div className="pt-8 flex gap-4">
                            <button type="submit"
                                className="bg-gradient-to-r from-blue-600 to-indigo-600 text-white px-10 py-4 rounded-lg font-bold text-lg shadow-lg hover:shadow-2xl transform hover:scale-105 transition">
                                Cập nhật khuyến mãi
                            </button>
                            <Link to="/admin/discounts"
                                className="px-10 py-4 bg-gray-300 text-gray-700 rounded-lg font-medium hover:bg-gray-400 transition">
                                Hủy
                            </Link>
                        </div>
                    </form>
                </div>
            </div >
        </div >
    );
};

export default AdminDiscountEdit;