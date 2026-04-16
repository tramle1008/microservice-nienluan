
import { useState, useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import toast from "react-hot-toast";
import { useForm } from "react-hook-form";
import { useNavigate } from "react-router-dom";
import api from "../../../api/api"; // axios instance
import { fetchAddresses } from "../../../store/actions";

const AddAddress = () => {
    const dispatch = useDispatch();
    const navigate = useNavigate();
    const { addresses } = useSelector((state) => state.address);

    const [provinces, setProvinces] = useState([]);
    const [wards, setWards] = useState([]);
    const [selectedProvince, setSelectedProvince] = useState("");
    const [loadingProvinces, setLoadingProvinces] = useState(false);
    const [loadingWards, setLoadingWards] = useState(false);

    const {
        register,
        handleSubmit,
        setValue,
        watch,
        formState: { errors },
    } = useForm();

    const watchedProvince = watch("provinceId");

    // Lấy danh sách tỉnh/thành
    useEffect(() => {
        const loadProvinces = async () => {
            setLoadingProvinces(true);
            try {
                const res = await api.get("/address/provinces");
                setProvinces(res.data);
            } catch (err) {
                toast.error("Không tải được danh sách tỉnh/thành");
                console.error(err);
            } finally {
                setLoadingProvinces(false);
            }
        };
        loadProvinces();
    }, []);

    // Khi chọn tỉnh → lấy phường/xã
    useEffect(() => {
        if (!watchedProvince) {
            setWards([]);
            setValue("wardId", "");
            return;
        }

        const loadWards = async () => {
            setLoadingWards(true);
            try {
                const res = await api.get(`/address/wards?provinceId=${watchedProvince}`);
                setWards(res.data);
                setValue("wardId", ""); // reset phường
            } catch (err) {
                toast.error("Không tải được danh sách phường/xã");
                console.error(err);
            } finally {
                setLoadingWards(false);
            }
        };
        loadWards();
    }, [watchedProvince, setValue]);

    // Lấy token
    const storedAuth = localStorage.getItem("auth");
    let token = null;
    try {
        const parsed = JSON.parse(storedAuth || "{}");
        token = parsed?.jwtToken;
    } catch { }

    const onSubmit = async (data) => {
        if (!token) {
            toast.error("Phiên đăng nhập hết hạn");
            navigate("/login");
            return;
        }

        try {
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

            toast.success("Thêm địa chỉ thành công!");
            dispatch(fetchAddresses());
            navigate(-1);
        } catch (err) {
            toast.error(err.response?.data?.message || "Thêm địa chỉ thất bại");
            console.error(err);
        }
    };

    return (
        <div className="bg-white p-6 rounded-lg shadow-md max-w-md mx-auto my-10">
            <h2 className="text-2xl font-bold mb-6 text-center">Thêm địa chỉ mới</h2>

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">

                {/* Tỉnh/Thành */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        Tỉnh/Thành phố <span className="text-red-500">*</span>
                    </label>
                    <select
                        {...register("provinceId", { required: "Vui lòng chọn tỉnh/thành" })}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-emerald-500"
                        disabled={loadingProvinces}
                        onChange={(e) => {
                            setValue("provinceId", e.target.value);
                            setValue("wardId", "");
                        }}
                    >
                        <option value="">
                            {loadingProvinces ? "Đang tải..." : "-- Chọn tỉnh/thành --"}
                        </option>
                        {provinces.map((prov) => (
                            <option key={prov.id} value={prov.id}>
                                {prov.name}
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
                        disabled={!watchedProvince || loadingWards}
                    >
                        <option value="">
                            {loadingWards
                                ? "Đang tải..."
                                : watchedProvince
                                    ? "-- Chọn phường/xã --"
                                    : "-- Chọn tỉnh trước --"}
                        </option>
                        {wards.map((ward) => (
                            <option key={ward.id} value={ward.id}>
                                {ward.name}
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
                            required: "Vui lòng nhập số điện thoại",
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

                {/* Nút submit */}
                <button
                    type="submit"
                    className="w-full bg-gray-700 text-white py-2.5 px-4 rounded-lg hover:bg-emerald-600 transition duration-300 font-medium"
                >
                    Thêm địa chỉ
                </button>
            </form>

            {/* Hiển thị danh sách địa chỉ hiện tại */}
            {addresses && addresses.length > 0 && (
                <div className="mt-8 p-4 bg-gray-50 rounded-lg">
                    <h3 className="font-semibold text-gray-800 mb-2">Địa chỉ hiện tại:</h3>
                    <ul className="space-y-1 text-sm text-gray-600">
                        {addresses.map((addr) => (
                            <li key={addr.addressId}>
                                • {addr.detail ? `${addr.detail}, ` : ""}
                                {addr.ward}, {addr.district || ""} {addr.province}
                            </li>
                        ))}
                    </ul>
                </div>
            )}
        </div>
    );
};

export default AddAddress;