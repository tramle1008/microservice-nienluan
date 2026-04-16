import { useState, useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { updateUser, fetchAddresses } from "../store/actions";
import InputField from "./InputField";

const UpdateAccountForm = () => {
    const dispatch = useDispatch();
    const { user } = useSelector((state) => state.auth);
    const { addresses } = useSelector((state) => state.address);

    const [password, setPassword] = useState("");
    const [newAddress, setNewAddress] = useState("");

    useEffect(() => {
        dispatch(fetchAddresses());
    }, [dispatch]);

    const handleSubmit = (e) => {
        e.preventDefault();

        const payload = {};
        if (password) payload.password = password;
        if (newAddress) payload.newAddress = newAddress;

        dispatch(updateUser(payload));
    };

    return (
        <div className="bg-white p-6 rounded shadow-md max-w-md mx-auto mt-10">
            <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                <InputField
                    label="Tên người dùng"
                    id="username"
                    value={user?.userName}
                    placeholder={user?.userName}
                    disabled={true}
                    register={() => ({})}
                    errors={{}}
                />

                <InputField
                    label="Email"
                    id="email"
                    type="email"
                    value={user?.email}
                    placeholder={user?.email}
                    disabled={true}
                    register={() => ({})}
                    errors={{}}
                />

                <InputField
                    label="Mật khẩu mới"
                    id="password"
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="Nhập mật khẩu mới"
                    register={() => ({})}
                    errors={{}}
                />

                <InputField
                    label="Thêm địa chỉ mới"
                    id="newAddress"
                    value={newAddress}
                    onChange={(e) => setNewAddress(e.target.value)}
                    placeholder="VD: 123 Đường ABC, P.XYZ"
                    register={() => ({})}
                    errors={{}}
                />

                <button
                    type="submit"
                    className="bg-blue-600 text-white py-2 rounded hover:bg-blue-700 transition"
                >
                    Cập nhật
                </button>
            </form>

            {addresses && addresses.length > 0 && (
                <div className="mt-6">
                    <h3 className="font-semibold mb-2">Địa chỉ hiện tại:</h3>
                    <ul className="space-y-1 text-sm text-gray-700">
                        {addresses.map((addr) => (
                            <li key={addr.addressId}>
                                • {addr.detail}, {addr.ward}, {addr.district}, {addr.province}
                            </li>
                        ))}
                    </ul>
                </div>
            )}
        </div>
    );
};

export default UpdateAccountForm;
