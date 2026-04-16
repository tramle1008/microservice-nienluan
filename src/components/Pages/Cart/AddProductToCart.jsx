import axios from "axios";
import { toast } from "react-hot-toast";
import api from "../../../api/api";

const AddProductToCart = async (productId, quantity = 1) => {
    try {
        const authData = localStorage.getItem("auth");

        if (!authData) {
            toast.error("Vui lòng đăng nhập trước khi thêm vào giỏ hàng!");
            return null;
        }

        const { jwtToken } = JSON.parse(authData);

        if (!jwtToken) {
            toast.error("Phiên đăng nhập đã hết hạn!");
            return null;
        }

        const response = await api.post(
            `/auth/cart/products/${productId}/quantity/${quantity}`,
            {}, // body rỗng
            {
                headers: {
                    Authorization: `Bearer ${jwtToken}`,
                },
            }
        );

        toast.success("Đã thêm sản phẩm vào giỏ hàng!");
        return response.data;
    } catch (err) {
        const errorMsg = err?.response?.data?.message || "Sản phẩm đã có sẳn trong giỏ hàng!";
        toast.error(errorMsg);
        console.error("Lỗi khi thêm vào giỏ:", err);
        return null;
    }
};

export default AddProductToCart;
