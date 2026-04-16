// src/components/Admin/HandleShip.jsx
import axios from "axios";
import toast from "react-hot-toast";
import api from "../../../api/api";

const HandleDelivered = ({ orderId, onSuccess }) => {
    const handleConfirm = async () => {
        try {
            const auth = JSON.parse(localStorage.getItem("auth"));
            const token = auth?.jwtToken;

            const response = await api.put(
                `/orders/${orderId}/delivered`,
                {},
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            toast.success("Đơn hàng đã được chuyển sang trạng thái 'Đã giao'");
            if (onSuccess) onSuccess(); // gọi callback nếu cần reload danh sách
        } catch (error) {
            console.error("Lỗi xác nhận giao hàng:", error);
            toast.error("Không thể xác nhận giao hàng!");
        }
    };

    return (
        <button
            onClick={handleConfirm}
            className="bg-[#506553] text-white px-3 py-1 rounded hover:bg-[#92a695] transition"
        >
            Xác nhận đã giao
        </button>
    );
};

export default HandleDelivered;
