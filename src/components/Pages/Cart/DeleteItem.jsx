import axios from "axios";
import toast from "react-hot-toast";
import api from "../../../api/api";

// /**
//  * Xóa một sản phẩm khỏi giỏ hàng
//  * @param {number} productId - ID của sản phẩm cần xóa
//  * @param {function} onSuccess - Callback khi xóa thành công (ví dụ: để làm mới giỏ hàng)
//  */
// const DeleteItem = async (productId, onSuccess) => {

//     const auth = JSON.parse(localStorage.getItem("auth"));

//     try {
//         const token = auth.jwtToken;
//         const response = await api.delete(
//             `/auth/user/cart/product/${productId}`,
//             {
//                 headers: {
//                     Authorization: `Bearer ${token}`,
//                 },
//             }
//         );

//         toast.success(response.data || "Đã xóa sản phẩm khỏi giỏ hàng");

//         // Gọi callback nếu được truyền
//         if (onSuccess) {
//             onSuccess();
//         }

//     } catch (error) {
//         console.error("Lỗi khi xóa sản phẩm:", error);
//         toast.error("Xóa sản phẩm thất bại");
//     }
// };
const DeleteItem = async (itemId, callback) => {
    const token = (() => {
        const auth = localStorage.getItem("auth");
        return auth ? JSON.parse(auth).jwtToken : null;
    })();

    if (!token) return;

    try {
        await api.delete(`/carts/items/${itemId}`, {
            headers: { Authorization: `Bearer ${token}` }
        });
        if (callback) callback();
    } catch (error) {
        console.error("Xóa thất bại:", error);
    }
};


export default DeleteItem;
