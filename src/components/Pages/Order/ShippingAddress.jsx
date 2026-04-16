import { Button } from "@headlessui/react";
import { Box, Typography } from "@mui/material";
import axios from "axios";
import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { fetchAddresses } from "../../../store/actions";
import { Link } from "react-router-dom";

const ShippingAddress = ({ onNext, setAddressId }) => {
    const dispatch = useDispatch();
    const [selectedId, setSelectedId] = useState(null);

    const { addresses, loading, error } = useSelector(state => state.address);

    useEffect(() => {
        dispatch(fetchAddresses());
    }, [dispatch]);

    const handleSelect = (id) => {
        setSelectedId(id);
        setAddressId(id);
    };

    return (
        <div className="flex justify-center">
            <Box className="w-full max-w-2xl mx-4 mb-10">
                <h1 className="text-center">Chọn địa chỉ giao hàng</h1>
                {loading && <p>Đang tải địa chỉ...</p>}
                {error && <p className="text-red-500">{error}</p>}
                {addresses.length > 0 && (
                    <ul className="space-y-2 mb-2 ">
                        {addresses.map(addr => (
                            <li key={addr.addressId}
                                className={`border p-3 rounded cursor-pointer ${selectedId === addr.addressId ? 'bg-green-100' : 'bg-gray-50'}`}
                                onClick={() => handleSelect(addr.addressId)}
                            >
                                <input
                                    type="radio"
                                    checked={selectedId === addr.addressId}
                                    onChange={() => handleSelect(addr.addressId)}
                                    className="mr-2"
                                />
                                {addr.detail ? `${addr.detail}, ` : ""}{addr.ward}, {addr.district}, {addr.province} <br />
                                SĐT: {addr.phoneNumber}
                            </li>
                        ))}
                    </ul>
                )}
                <div className="mb-6">
                    <Link to="/user/update/address" >
                        <button className="flex items-center font-bold px-4 py-1 border border-b-blue-950 rounded-md hover:bg-blue-100 transition-colors duration-200"
                        >
                            + Thêm địa chỉ
                        </button>
                    </Link>

                </div>
                <Button
                    disabled={!selectedId}
                    onClick={onNext}
                    className={`px-6 py-2 rounded-md font-semibold transition duration-300
                         ${selectedId
                            ? "text-blue-700 border border-blue-600 rounded-md bg-blue-100 hover:bg-blue-600 hover:text-white transition-colors duration-200 px-6 py-2 font-semibold"
                            : "bg-gray-300 text-gray-500 cursor-not-allowed"
                        }`}
                >
                    Tiếp tục
                </Button>
            </Box>
        </div>

    );
};

export default ShippingAddress;