
import React, { useEffect, useState } from "react";
import toast from "react-hot-toast";
import { useParams, useNavigate } from "react-router-dom";
import api from "../../../../api/api";
import AdminSidebar from "../AdminSidebar";


const EditShippingFee = () => {
    const { provinceId } = useParams();
    const navigate = useNavigate();

    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [provinceName, setProvinceName] = useState("");
    const getToken = () => JSON.parse(localStorage.getItem("auth"))?.jwtToken;

    const [formData, setFormData] = useState({
        defaultFee: "",
        freeShippingThreshold: "",
    });

    // Lấy thông tin phí hiện tại
    useEffect(() => {
        const fetchFee = async () => {
            try {
                setLoading(true);

                const token = getToken();
                const res = await api.get(
                    `/shipping/fee/all`,
                    {
                        headers: {
                            'Authorization': token ? `Bearer ${token}` : '',
                        }
                    }
                );
                const feeData = res.data.content.find(
                    (item) => item.provinceId === Number(provinceId)
                );

                if (!feeData) {
                    toast.error("Không tìm thấy tỉnh này!");
                    navigate("/admin/shipping");
                    return;
                }

                setProvinceName(feeData.provinceName);
                setFormData({
                    defaultFee: feeData.defaultFee || 0,
                    freeShippingThreshold: feeData.freeShippingThreshold || 0,
                });
            } catch (err) {
                console.error(err);
                toast.error("Lỗi tải thông tin phí vận chuyển");
            } finally {
                setLoading(false);
            }
        };

        fetchFee();
    }, [provinceId, navigate]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData((prev) => ({
            ...prev,
            [name]: value === "" ? "" : Number(value),
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (formData.defaultFee < 0 || formData.freeShippingThreshold < 0) {
            toast.error("Giá trị không được âm!");
            return;
        }

        try {
            setSaving(true);
            const payload = {
                defaultFee:
                    formData.defaultFee === "" ? null : Number(formData.defaultFee),
                freeShippingThreshold:
                    formData.freeShippingThreshold === ""
                        ? null
                        : Number(formData.freeShippingThreshold),
            };
            const token = getToken();
            const res = await api.put(`/shipping/fee/${provinceId}`, payload,
                {
                    headers: {
                        'Authorization': token ? `Bearer ${token}` : '',
                    }
                }
            );


            toast.success("Cập nhật phí vận chuyển thành công!");
            navigate("/admin/shipping"); // Quay lại danh sách
        } catch (err) {
            console.error(err);
            toast.error("Cập nhật thất bại. Vui lòng thử lại.");
        } finally {
            setSaving(false);
        }
    };

    if (loading) {
        return (
            <div className="flex min-h-screen bg-gray-100">
                <AdminSidebar />
                <div className="flex-1 p-8 flex items-center justify-center">
                    <p className="text-xl text-gray-600">Đang tải thông tin...</p>
                </div>
            </div>
        );
    }

    return (
        <div className="flex min-h-screen bg-gray-100">
            <AdminSidebar />

            <main className="flex-1 p-8">
                <div className="max-w-2xl mx-auto">
                    <div className="bg-white rounded-xl shadow-lg p-8">
                        <div className=" gap-15 flex items-center my-10">
                            <button
                                onClick={() => navigate("/admin/shipping")}
                                className="text-gray-500 hover:text-gray-700 transition bg-gray-100 rounded-md px-3 py-2"
                            >
                                Quay lại
                            </button>
                            <h1 className="text-3xl font-bold text-gray-800">
                                Cập nhật phí vận chuyển
                            </h1>

                        </div>

                        <div className="mb-8 p-6 bg-emerald-50 rounded-lg border border-emerald-200">
                            <h2 className="text-2xl font-bold text-green-800">
                                {provinceName}
                            </h2>
                            <p className="text-sm text-green-600 mt-1">
                                ID tỉnh: {provinceId}
                            </p>
                        </div>

                        <form onSubmit={handleSubmit} className="space-y-6">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    Phí vận chuyển mặc định (₫)
                                </label>
                                <input
                                    type="number"
                                    name="defaultFee"
                                    value={formData.defaultFee}
                                    onChange={handleChange}
                                    min="0"
                                    step="1000"
                                    placeholder="Ví dụ: 25000"
                                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-lg"
                                    required
                                />
                                <p className="text-xs text-gray-500 mt-1">
                                    Để trống = giữ nguyên giá trị cũ
                                </p>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    Miễn phí vận chuyển từ (₫)
                                    <span className="text-gray-500 text-xs ml-2">
                                        (khách mua từ số tiền này sẽ được miễn phí ship)
                                    </span>
                                </label>
                                <input
                                    type="number"
                                    name="freeShippingThreshold"
                                    value={formData.freeShippingThreshold}
                                    onChange={handleChange}
                                    min="0"
                                    step="10000"
                                    placeholder="Ví dụ: 300000"
                                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 text-lg"
                                />
                                <p className="text-xs text-gray-500 mt-1">
                                    Để trống = giữ nguyên, hoặc 0 = không miễn phí
                                </p>
                            </div>

                            <div className="flex gap-4 pt-6">
                                <button
                                    type="submit"
                                    disabled={saving}
                                    className={`flex-1 py-3 rounded-lg font-bold text-white transition ${saving
                                        ? "bg-gray-400 cursor-not-allowed"
                                        : "bg-blue-600 hover:bg-blue-700"
                                        }`}
                                >
                                    {saving ? "Đang lưu..." : "Lưu thay đổi"}
                                </button>

                            </div>
                        </form>
                    </div>
                </div>
            </main>
        </div>
    );
};

export default EditShippingFee;