
import axios from "axios";
import toast from "react-hot-toast";
import api from "../../../api/api";

const HandleReject = ({ orderId, onSuccess }) => {
    const handleReject = async () => {
        try {
            const auth = JSON.parse(localStorage.getItem("auth"));
            const token = auth?.jwtToken;

            const response = await api.put(
                `/orders/admin/cancel/${orderId}`,
                {},
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            toast.success("Đơn hàng đã bị từ chối");
            if (onSuccess) onSuccess();
        } catch (error) {
            console.error("Lỗi xác nhận từ chối", error);
            toast.error("Không thể xác từ chối!");
        }
    };

    return (
        <button
            onClick={handleReject}
            className="bg-red-600 text-white px-3 py-1 rounded hover:bg-red-300 transition"
        >
            Từ chối giao hàng
        </button>
    );
};

export default HandleReject;
