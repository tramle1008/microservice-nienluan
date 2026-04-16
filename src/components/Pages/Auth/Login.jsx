// Login.jsx
import { useForm } from "react-hook-form";
import axios from "axios";
import { toast } from "react-hot-toast";

import InputField from "../../InputField";
import { Link, useNavigate } from "react-router-dom";
import { useDispatch } from "react-redux";
import api from "../../../api/api";

const Login = () => {
    const { register, handleSubmit, formState: { errors } } = useForm();
    const navigate = useNavigate();
    const dispatch = useDispatch();
    const handleLogin = async (data) => {
        try {
            const res = await axios.post("http://localhost:8080/api/auth/signin", {
                username: data.username,
                password: data.password,
            }, {
                headers: {
                    "Content-Type": "application/json",
                },
            });

            const user = res.data;

            if (user && user.userName) {
                toast.success("Đăng nhập thành công!");
                localStorage.setItem("auth", JSON.stringify(user));

                dispatch({ type: "LOGIN_SUCCESS", payload: user });

                const roles = user.role || [];
                const isAdmin = roles.includes("ROLE_ADMIN");

                if (isAdmin) {
                    navigate("/admin");
                } else {
                    navigate("/");
                }

            } else {
                toast.error("Đăng nhập thất bại.");
            }
        } catch (err) {
            toast.error("Đăng nhập lỗi: " + (err.response?.data?.message || "Lỗi kết nối"));
        }

    };

    return (
        <div className="max-w-md mx-auto my-20 bg-white shadow-lg p-6 rounded-xl border">
            <h2 className="text-2xl font-bold mb-6 text-center text-gray-800">Đăng nhập</h2>

            <form onSubmit={handleSubmit(handleLogin)} className="flex flex-col gap-4">
                <InputField
                    label="Tên đăng nhập"
                    id="username"
                    type="text"
                    placeholder="Nhập username"
                    register={register}
                    errors={errors}
                    required={true}
                    message="Không được để trống"
                />

                <InputField
                    label="Mật khẩu"
                    id="password"
                    type="password"
                    placeholder="Nhập mật khẩu"
                    register={register}
                    errors={errors}
                    required={true}
                    message="Không được để trống"
                    min={8}
                />

                <button
                    type="submit"
                    className="bg-emerald-700 text-white py-2 px-4 rounded-lg hover:bg-emerald-900 transition duration-300"
                >
                    Đăng nhập
                </button>
                <span>Bạn chưa có tài khoản
                    <Link to="/register" className="text-cyan-600 hover:underline"> Đăng ký</Link>
                </span>
            </form>
        </div>
    );
};

export default Login;
