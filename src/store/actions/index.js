//// src/store/actions/index.js
import toast from "react-hot-toast";
import api from "../../api/api";
import axios from "axios";
export const fetchProduct = (categoryId, queryString) => async (dispatch) => {
    try {
        const endpoint = `/products/public?${queryString}`;

        const { data } = await api.get(endpoint);

        dispatch({
            type: "FETCH_PRODUCTS",
            payload: {
                products: data.content,
                pageNumber: data.pageable?.pageNumber,
                pageSize: data.pageable?.pageSize,
                totalElements: data.totalElements,
                totalPages: data.totalPages,
                lastPage: data.last,
            },
        });
    } catch (error) {
        console.error("Lỗi khi fetch sản phẩm:", error);
    }
};

// src/store/actions/categoriesProductActions.js
export const fetchCategoryTreeProducts = (categoryId, queryString = "") => async (dispatch) => {
    dispatch({ type: "FETCH_CATEGORY_TREE_PRODUCTS_START" });

    try {
        const qs = queryString ? (queryString.startsWith("?") ? queryString : `?${queryString}`) : "";
        const endpoint = `products/public/category/${categoryId}${qs}`;
        const { data } = await api.get(endpoint);

        dispatch({
            type: "FETCH_CATEGORY_TREE_PRODUCTS_SUCCESS",
            payload: {
                products: data.content,
                pagination: {
                    pageNumber: data.pageNumber,
                    pageSize: data.pageSize,
                    totalElements: data.totalElements,
                    totalPages: data.totalPages,
                    lastPage: data.lastPage,
                },
                categoryId,
            },
        });
    } catch (error) {
        dispatch({
            type: "FETCH_CATEGORY_TREE_PRODUCTS_ERROR",
            payload: error.response?.data?.message || error.message,
        });
    }
};

// store/actions/discountActions.js (hoặc thêm vào productActions.js)

export const applyDiscountToProduct = (productId, discountId) => async (dispatch) => {
    try {
        const auth = JSON.parse(localStorage.getItem("auth"));
        const token = auth?.jwtToken;
        await api.post(
            `/discounts/create/apply/product`,
            { productId, discountId },
            {
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
            }
        );

        toast.success("Gán khuyến mãi cho sản phẩm thành công!");
        dispatch(fetchProduct(null, `pageNumber=0&pageSize=5&sortBy=productId&sortOrder=asc`));
        return { success: true };
    } catch (error) {
        console.error("Lỗi gán KM sản phẩm:", error.response?.data);
        toast.error(error.response?.data?.message || "Không thể gán khuyến mãi");
        return { success: false, error: error.response?.data?.message || "Lỗi server" };
    }
};

export const applyDiscountToVariant = (variantId, discountId) => async (dispatch) => {
    try {
        const auth = JSON.parse(localStorage.getItem("auth"));
        const token = auth?.jwtToken;

        await api.post(
            `/discounts/create/apply/variant`,
            { variantId, discountId },
            {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            }
        );

        toast.success("Gán khuyến mãi cho biến thể thành công!");
        dispatch(fetchProduct(null, `pageNumber=0&pageSize=5&sortBy=productId&sortOrder=asc`));
        return { success: true };
    } catch (error) {
        console.error("Lỗi gán KM variant:", error.response?.data);
        toast.error(error.response?.data?.message || "Không thể gán khuyến mãi");
        return { success: false, error: error.response?.data?.message || "Lỗi server" };
    }
};

export const clearCategoryTreeProducts = () => ({
    type: "CLEAR_CATEGORY_TREE_PRODUCTS",
});

export const fetchCategories = () => async (dispatch) => {
    try {
        const { data } = await api.get(`/categories/public`);

        dispatch({
            type: "FETCH_CATEGORIES",
            payload: data.content,
        });
    } catch (error) {
        console.error("Lỗi khi tải danh mục:", error);
    }
};

export const fetchRoot = () => async (dispatch) => {
    try {
        const { data } = await api.get('/categories/public/tree');
        console.log("API /tree response:", data); // THÊM DÒNG NÀY
        dispatch({
            type: 'FETCH_CATEGORIES_TREE', // lưu vào Redux
            payload: data,   // ← mảng cây danh mục
        });
    } catch (error) {
        console.error('Lỗi tải danh mục:', error);
    }
};

export const searchProducts = async (keyword, pageNumber = 0, pageSize = 10) => {
    try {
        const response = await api.get(`/products/public/search`, {
            params: { keyword, pageNumber, pageSize }
        });
        return response.data; // trả về object chứa content, pageNumber, ...
    } catch (error) {
        console.error("Lỗi khi tải sản phẩm:", error);
        return null;
    }
};

export const fetchAddresses = () => async (dispatch) => {
    dispatch({ type: "ADDRESS_FETCH_REQUEST" });

    try {
        const auth = JSON.parse(localStorage.getItem("auth"));
        const token = auth?.jwtToken;

        const res = await api.get("/address/user", {
            headers: {
                Authorization: `Bearer ${token}`
            }
        });

        dispatch({
            type: "ADDRESS_FETCH_SUCCESS",
            payload: res.data
        });
    } catch (error) {
        dispatch({
            type: "ADDRESS_FETCH_FAIL",
            payload: error.response?.data?.message || error.message,
        });
    }
};



//fetchCart
export const fetchCart = () => async (dispatch) => {
    dispatch({ type: "CART_REQUEST" });

    try {
        const auth = localStorage.getItem("auth");
        const token = auth ? JSON.parse(auth).jwtToken : null;
        if (!token) {
            console.warn("Cảnh báo: Chưa đăng nhập – Giỏ hàng sẽ được để trống.");
            dispatch({
                type: "CART_SUCCESS",
                payload: { items: [] }, // Dữ liệu rỗng
            });
            return;
        }
        const res = await api.get("/carts/my", {
            headers: {
                Authorization: `Bearer ${token}`,
            },
        });

        dispatch({
            type: "CART_SUCCESS",
            payload: res.data,
        });
    } catch (err) {
        if (err.response?.status === 401) {
            console.warn("Cảnh báo: Phiên đăng nhập hết hạn (401) -  Giỏ hàng được đặt về rỗng.");
            dispatch({
                type: "CART_SUCCESS",
                payload: { items: [] }, // Vẫn thành công, nhưng giỏ hàng rỗng
            });
        } else {
            // Các lỗi khác (500, network, v.v.) mới báo lỗi thật
            dispatch({
                type: "CART_FAILURE",
                payload: err.response?.data?.message || "Lỗi tải giỏ hàng",
            });
        }
    }
};

const getToken = () => {
    const auth = localStorage.getItem("auth");
    const token = auth ? JSON.parse(auth).jwtToken : null;
    return token;
};

export const addToCart = (variantId, quantity = 1) => async (dispatch) => {
    const token = getToken();
    if (!token) {
        toast.error("Vui lòng đăng nhập!");
        return;
    }

    try {
        const response = await api.post(
            '/carts/items',
            { variantId, quantity },
            {
                headers: {
                    Authorization: `Bearer ${token}`
                }
            }
        );

        // Cập nhật Redux với giỏ hàng mới từ server
        dispatch({
            type: 'UPDATE_CART',
            payload: response.data
        });

        toast.success(`Đã thêm vào giỏ (x${quantity})`);

    } catch (error) {
        const msg = error.response?.data?.message || 'Bạn chưa đăng nhập';
        toast.error(msg);
    }
};
export const incrementItem = (itemId) => async (dispatch) => {
    const token = getToken();
    if (!token) return;

    await api.post(`/carts/items/${itemId}/increment`, {}, {
        headers: { Authorization: `Bearer ${token}` }
    });
    dispatch(fetchCart());
};

export const decrementItem = (itemId) => async (dispatch) => {
    const token = getToken();
    if (!token) return;

    await api.post(`/carts/items/${itemId}/decrement`, {}, {
        headers: { Authorization: `Bearer ${token}` }
    });
    dispatch(fetchCart());
};
// ////
export const updateUser = (userData) => async (dispatch, getState) => {
    dispatch({ type: "USER_UPDATE_REQUEST" });

    try {

        const auth = localStorage.getItem("auth");
        const token = JSON.parse(auth)?.jwtToken;

        const res = await api.put('/auth/user/update', userData, {
            headers: {
                Authorization: `Bearer ${token}`,
            },
        });

        dispatch({
            type: "USER_UPDATE_SUCCESS",
            payload: res.data,
        });
    } catch (error) {
        dispatch({
            type: "USER_UPDATE_FAIL",
            payload: error.response?.data?.message || error.message,
        });
    }
};
export const checkShippingFeeByAddress = (addressId) => async (dispatch) => {
    dispatch({ type: "SHIPPING_FEE_REQUEST" });

    try {
        const auth = JSON.parse(localStorage.getItem("auth") || "{}");
        const token = auth?.jwtToken;

        const res = await api.get(`/shipping/fee/address/${addressId}`, {
            headers: {
                Authorization: `Bearer ${token}`,
            }
        });

        dispatch({
            type: "SHIPPING_FEE_SUCCESS",
            payload: res.data,   // JSON fee ship
        });
    } catch (error) {
        dispatch({
            type: "SHIPPING_FEE_FAIL",
            payload: error.response?.data?.message || error.message,
        });
    }
};

// export const fetchOrders = () => async (dispatch) => {
//     dispatch({ type: "ORDER_FETCH_REQUEST" });

//     try {
//         const auth = JSON.parse(localStorage.getItem("auth") || "{}");
//         const token = auth?.jwtToken;

//         if (!token) throw new Error("Không tìm thấy token");

//         const res = await api.get("/orders", {
//             headers: {
//                 Authorization: `Bearer ${token}`,
//             },
//         });

//         dispatch({
//             type: "ORDER_FETCH_SUCCESS",
//             payload: {
//                 orders: res.data.content,
//                 totalPages: res.data.totalPages,
//                 currentPage: res.data.number,
//             },
//         });
//     } catch (error) {
//         dispatch({
//             type: "ORDER_FETCH_FAIL",
//             payload: error.response?.data?.message || error.message,
//         });
//     }
// };


export const fetchUserOrders = (
    page = 0,
    size = 2,
    sortBy = "orderId",
    sortDir = "desc"
) => async (dispatch) => {
    dispatch({ type: "ORDER_USER_REQUEST" });

    try {
        const auth = JSON.parse(localStorage.getItem("auth"));
        const token = auth?.jwtToken; // phải đồng bộ tên field với backend

        const res = await api.get(
            `/orders?page=${page}&size=${size}&sortBy=${sortBy}&sortDir=${sortDir}`,
            {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            }
        );

        dispatch({
            type: "ORDER_USER_SUCCESS",
            payload: {
                orders: res.data.content,
                totalPages: res.data.totalPages,
                totalElements: res.data.totalElements,
                pageNumber: res.data.pageable?.pageNumber,
                pageSize: res.data.pageable?.pageSize,
                lastPage: res.data.last,
            },
        });
    } catch (error) {
        dispatch({
            type: "ORDER_USER_FAILURE",
            payload: error.response?.data?.message || error.message,
        });
    }
};
export const fetchDiscountedProducts = () => async (dispatch) => {
    dispatch({ type: "DISCOUNTED_PRODUCTS_REQUEST" });

    try {
        // Không cần token → gọi API public
        const res = await api.get('/products/public/discounted');

        dispatch({
            type: "DISCOUNTED_PRODUCTS_SUCCESS",
            payload: res.data,          // mảng sản phẩm giảm giá
        });
    } catch (error) {
        dispatch({
            type: "DISCOUNTED_PRODUCTS_FAIL",
            payload:
                error.response?.data?.message ||
                error.message ||
                'Không thể tải sản phẩm giảm giá',
        });
    }
};

// chưa

export const fetchAdminOrders = (queryString) => async (dispatch) => {
    dispatch({ type: "ORDER_FETCH_REQUEST" });

    try {
        const auth = JSON.parse(localStorage.getItem("auth"));
        const token = auth?.jwtToken;

        const res = await api.get(`/admin/orders?${queryString}`, {
            headers: {
                Authorization: `Bearer ${token}`,
            },
        });

        dispatch({
            type: "ORDER_FETCH_SUCCESS",
            payload: {
                orders: res.data.content,
                pageNumber: res.data.pageNumber,
                pageSize: res.data.pageSize,
                totalElements: res.data.totalElements,
                totalPages: res.data.totalPages,
                lastPage: res.data.lastPage,
            },
        });
    } catch (error) {
        dispatch({
            type: "ORDER_FETCH_FAIL",
            payload: error.response?.data?.message || error.message,
        });
    }
};
