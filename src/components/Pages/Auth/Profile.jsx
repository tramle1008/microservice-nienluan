import { useEffect, useState } from "react";
import axios from "axios";
import { Link, useNavigate } from "react-router-dom";
import UpdateAccountForm from "../../UpdateAccountForm";
import { MdDelete } from "react-icons/md";
import toast from "react-hot-toast";
import api from "../../../api/api";

const Profile = () => {
    const navigate = useNavigate();
    const [addresses, setAddresses] = useState(null);
    const [auth, setAuth] = useState(null);
    const [error, setError] = useState(null);
    const [user, setUser] = useState(null);
    useEffect(() => {
        const storedAuth = localStorage.getItem("auth");
        let token = null;

        try {
            const parsedAuth = JSON.parse(storedAuth);
            setAuth(parsedAuth);
            token = parsedAuth?.jwtToken;
        } catch {
            setError("Token không hợp lệ");
            return;
        }

        if (!token) {
            setError("Chưa đăng nhập hoặc token không tồn tại");
            return;
        }

        const headers = { Authorization: `Bearer ${token}` };

        api.get("/address/user", { headers })
            .then(res => setAddresses(res.data))
            .catch(err => {
                console.error(err);
            });
    }, []);

    const handleDelete = async (addressId) => {
        if (!auth?.jwtToken) {
            setError("Không tìm thấy token xác thực.");
            return;
        }

        const headers = { Authorization: `Bearer ${auth.jwtToken}` };

        try {
            await api.delete(`/address/${addressId}`, { headers });
            toast.success("Cập nhật thành công")
            setAddresses(prev => prev.filter(addr => addr.addressId !== addressId));

        } catch (err) {
            console.error("Lỗi khi xóa địa chỉ:", err);
            toast.error("Bạn không thể xóa tất cả địa chỉ")
        }
    };


    if (error) return <p className="text-red-500 mt-10 text-center">{error}</p>;

    if (!auth) {
        return <p className="text-center text-gray-600 mt-10">Đang tải dữ liệu...</p>;
    }

    return (
        <div className="max-w-4xl mx-auto my-16 bg-white shadow-lg rounded-lg p-6">
            <h2 className="text-2xl font-bold mb-4  text-center">Hồ sơ người dùng</h2>
            <ul className="space-y-2 mb-6">
                <li><strong>Tên đăng nhập:</strong> {auth?.userName}</li>
                <li><strong>Email:</strong> {auth?.email}</li>
                <li className="text-gray-400">Không thể đổi tên đăng nhập và mật khẩu</li>
            </ul>

            {Array.isArray(addresses) && addresses.length > 0 && (
                <>
                    <h3 className="text-xl font-semibold mt-8 mb-4">Địa chỉ giao hàng</h3>
                    <ul className="space-y-2 mb-6">
                        {addresses.map(addr => (
                            <li key={addr.addressId} className="border p-3 rounded bg-gray-50 flex justify-between items-start">
                                <div>
                                    {addr.detail ? `${addr.detail}, ` : ""} {addr.ward}, {addr.district}, {addr.province} <br />
                                    SĐT: {addr.phoneNumber}
                                </div>
                                <div className="text-gray-800 hover:text-rose-600 cursor-pointer">
                                    <MdDelete size={25} onClick={() => handleDelete(addr.addressId)} />
                                </div>

                            </li>
                        ))}
                    </ul>
                </>
            )}


            <div className="flex gap-2">
                <Link to="/user/update/address" >
                    <button className="flex items-center font-bold px-4 py-1 border border-b-blue-950 rounded-md hover:bg-blue-100 transition-colors duration-200"
                    >
                        +  Thêm địa chỉ
                    </button>
                </Link>
                <Link to="/user/update/password" >
                    <button className="flex items-center font-bold px-4 py-1 border border-b-blue-950 rounded-md hover:bg-amber-100 transition-colors duration-200"
                    >
                        Đổi mật khẩu
                    </button>
                </Link>

            </div>
        </div>
    );
};

export default Profile;
