import { useState, useEffect } from "react";
import { useForm } from "react-hook-form";
import { toast } from "react-hot-toast";
import { useNavigate } from "react-router-dom";
import api from "../../../api/api";

const Register = () => {
    const [provinces, setProvinces] = useState([]);
    const [wards, setWards] = useState([]);
    const [loadingProvinces, setLoadingProvinces] = useState(false);
    const [loadingWards, setLoadingWards] = useState(false);

    const {
        register,
        handleSubmit,
        watch,
        setValue,
        formState: { errors },
    } = useForm();

    const navigate = useNavigate();
    const [selectedProvinceId, setSelectedProvinceId] = useState("");

    //  hàm:
    const handleProvinceChange = (e) => {
        const value = e.target.value;
        setSelectedProvinceId(value);
        setValue("provinceId", value);
        setValue("wardId", "");
    };

    // Tải danh sách tỉnh/thành
    useEffect(() => {
        const loadProvinces = async () => {
            setLoadingProvinces(true);
            try {
                const res = await api.get("/address/provinces");
                setProvinces(res.data);
            } catch (err) {
                toast.error("Không tải được danh sách tỉnh/thành");
            } finally {
                setLoadingProvinces(false);
            }
        };
        loadProvinces();
    }, []);

    // Khi chọn tỉnh → tải phường/xã
    // useEffect load wards
    useEffect(() => {
        if (!selectedProvinceId) {
            setWards([]);
            setValue("wardId", "");
            return;
        }

        const loadWards = async () => {
            setLoadingWards(true);
            try {
                const res = await api.get(`/address/wards?provinceId=${selectedProvinceId}`);
                setWards(res.data);
            } catch (err) {
                toast.error("Không tải được danh sách phường/xã");
            } finally {
                setLoadingWards(false);
            }
        };
        loadWards();
    }, [selectedProvinceId, setValue]);

    // Xử lý đăng ký
    const onSubmit = async (data) => {
        try {
            // 1. Đăng ký
            await api.post("/auth/signup", {
                username: data.username,
                email: data.email,
                password: data.password,
            });

            // 2. Tự động đăng nhập để lấy token
            const loginRes = await api.post("/auth/signin", {
                username: data.username,
                password: data.password,
            });

            const token = loginRes.data?.jwtToken;
            if (!token) throw new Error("Không nhận được token");

            // 3. Thêm địa chỉ
            await api.post(
                "/address/user",
                {
                    provinceId: Number(data.provinceId),
                    wardId: Number(data.wardId),
                    detail: data.detail.trim(),
                    phoneNumber: data.phoneNumber.trim(),
                },
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                        "Content-Type": "application/json",
                    },
                }
            );

            toast.success("Đăng ký và thêm địa chỉ thành công!");
            navigate("/login");
        } catch (error) {
            const msg = error.response?.data?.message || "Đăng ký thất bại";
            toast.error(msg);
            console.error("Lỗi:", error);
        }
    };

    return (
        <div className="max-w-md mx-auto mt-10 mb-20 bg-white shadow-lg p-6 rounded-xl border">
            <h2 className="text-2xl font-bold mb-6 text-center text-gray-800">
                Đăng ký tài khoản
            </h2>

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">

                {/* Tên người dùng */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        Tên người dùng <span className="text-red-500">*</span>
                    </label>
                    <input
                        type="text"
                        placeholder="Nhập tên đăng nhập"
                        {...register("username", {
                            required: "Tên người dùng không được để trống",
                            minLength: { value: 3, message: "Tối thiểu 3 ký tự" },
                        })}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-emerald-500"
                    />
                    {errors.username && (
                        <p className="text-red-500 text-xs mt-1">{errors.username.message}</p>
                    )}
                </div>

                {/* Email */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        Email <span className="text-red-500">*</span>
                    </label>
                    <input
                        type="email"
                        placeholder="you@example.com"
                        {...register("email", {
                            required: "Email không được để trống",
                            pattern: {
                                value: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
                                message: "Email không hợp lệ",
                            },
                        })}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-emerald-500"
                    />
                    {errors.email && (
                        <p className="text-red-500 text-xs mt-1">{errors.email.message}</p>
                    )}
                </div>

                {/* Mật khẩu */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        Mật khẩu <span className="text-red-500">*</span>
                    </label>
                    <input
                        type="password"
                        placeholder="••••••••"
                        {...register("password", {
                            required: "Mật khẩu không được để trống",
                            minLength: { value: 8, message: "Mật khẩu ít nhất 8 ký tự" },
                        })}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-emerald-500"
                    />
                    {errors.password && (
                        <p className="text-red-500 text-xs mt-1">{errors.password.message}</p>
                    )}
                </div>

                {/* Tỉnh/Thành */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        Tỉnh/Thành phố <span className="text-red-500">*</span>
                    </label>
                    <select
                        {...register("provinceId", { required: "Vui lòng chọn tỉnh/thành" })}
                        onChange={handleProvinceChange}
                        value={selectedProvinceId}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-emerald-500"
                        disabled={loadingProvinces}
                    >
                        <option value="">
                            {loadingProvinces ? "Đang tải..." : "-- Chọn tỉnh/thành --"}
                        </option>
                        {provinces.map((p) => (
                            <option key={p.id} value={p.id}>
                                {p.name}
                            </option>
                        ))}
                    </select>
                    {errors.provinceId && (
                        <p className="text-red-500 text-xs mt-1">{errors.provinceId.message}</p>
                    )}
                </div>

                {/* Phường/Xã */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        Phường/Xã <span className="text-red-500">*</span>
                    </label>
                    <select
                        {...register("wardId", { required: "Vui lòng chọn phường/xã" })}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-emerald-500"
                        disabled={!selectedProvinceId || loadingWards}
                    >
                        <option value="">
                            {loadingWards
                                ? "Đang tải..."
                                : selectedProvinceId
                                    ? "-- Chọn phường/xã --"
                                    : "-- Chọn tỉnh trước --"}
                        </option>
                        {wards.map((w) => (
                            <option key={w.id} value={w.id}>
                                {w.name}
                            </option>
                        ))}
                    </select>
                    {errors.wardId && (
                        <p className="text-red-500 text-xs mt-1">{errors.wardId.message}</p>
                    )}
                </div>

                {/* Địa chỉ chi tiết */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        Số nhà, tên đường <span className="text-red-500">*</span>
                    </label>
                    <input
                        type="text"
                        placeholder="123 Nguyễn Huệ, Quận 1"
                        {...register("detail", { required: "Vui lòng nhập địa chỉ chi tiết" })}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-emerald-500"
                    />
                    {errors.detail && (
                        <p className="text-red-500 text-xs mt-1">{errors.detail.message}</p>
                    )}
                </div>

                {/* Số điện thoại */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        Số điện thoại <span className="text-red-500">*</span>
                    </label>
                    <input
                        type="tel"
                        placeholder="0901234567"
                        {...register("phoneNumber", {
                            required: "Số điện thoại không được để trống",
                            pattern: {
                                value: /^[0-9]{10,11}$/,
                                message: "Số điện thoại không hợp lệ (10-11 số)",
                            },
                        })}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-emerald-500"
                    />
                    {errors.phoneNumber && (
                        <p className="text-red-500 text-xs mt-1">{errors.phoneNumber.message}</p>
                    )}
                </div>

                {/* Nút đăng ký */}
                <button
                    type="submit"
                    className="w-full bg-emerald-600 text-white py-2.5 px-4 rounded-lg hover:bg-emerald-700 transition duration-300 font-medium text-lg"
                >
                    Đăng ký
                </button>

                <p className="text-center text-sm text-gray-600 mt-4">
                    Đã có tài khoản?{" "}
                    <span
                        onClick={() => navigate("/login")}
                        className="text-emerald-600 font-medium cursor-pointer hover:underline"
                    >
                        Đăng nhập
                    </span>
                </p>
            </form>
        </div>
    );
};

export default Register;