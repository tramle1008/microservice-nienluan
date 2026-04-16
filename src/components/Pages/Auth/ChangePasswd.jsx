import { useState, useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { fetchAddresses, updateUser } from "../../../store/actions";
import InputField from "../../InputField";
import toast from "react-hot-toast";
import { useNavigate } from "react-router-dom";

const ChangePasswd = () => {
    const dispatch = useDispatch();
    const { user } = useSelector((state) => state.auth);

    const [password, setPassword] = useState("");
    const [showPassword, setShowPassword] = useState(false);
    const navigate = useNavigate();
    const handleSubmit = (e) => {


        e.preventDefault();
        if (!password) return;

        const payload = { password };

        dispatch(updateUser(payload));
        toast.success("Cập nhật mật khẩu thành công!");
        navigate(-1);
    };

    return (
        <div className="bg-white p-6 rounded shadow-md max-w-md mx-auto mt-10">
            <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                <div className="relative">
                    <label htmlFor="password" className="block mb-1 font-medium text-gray-700">
                        Mật khẩu mới
                    </label>
                    <input
                        id="password"
                        type={showPassword ? "text" : "password"}
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        placeholder="Nhập mật khẩu mới"
                        className="w-full border border-gray-300 p-2 rounded-md focus:outline-none focus:ring-2 focus:ring-emerald-500"
                        minLength={6}
                    />
                    <div className="text-sm mt-1 text-right">
                        <button
                            type="button"
                            className="text-emerald-600 hover:underline"
                            onClick={() => setShowPassword(!showPassword)}
                        >
                            {showPassword ? "Ẩn mật khẩu" : "Hiện mật khẩu"}
                        </button>
                    </div>
                </div>

                <button
                    type="submit"
                    className="bg-gray-600 text-white py-2 px-4 rounded-lg hover:bg-emerald-700 transition duration-300"
                >
                    Cập nhật
                </button>
            </form>
        </div>
    );
};

export default ChangePasswd;
