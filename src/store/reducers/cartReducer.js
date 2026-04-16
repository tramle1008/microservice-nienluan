// src/store/reducers/cartReducer.js
const initialState = {
    items: [],
    totalItems: 0,
    totalPrice: 0, // sẽ được tính từ total
    loading: false,
    error: null
};

export const cartReducer = (state = initialState, action) => {
    switch (action.type) {
        case 'CART_REQUEST':
            return { ...state, loading: true, error: null };

        case 'CART_SUCCESS':
        case 'UPDATE_CART':
            const payload = action.payload;

            // Tính totalItems từ items
            const totalItems = payload.items?.reduce((sum, item) => sum + item.quantity, 0) || 0;

            return {
                ...state,
                loading: false,
                items: payload.items || [],
                totalItems,
                totalPrice: Number(payload.total) || 0 // API trả về "total"
            };

        case 'CART_FAILURE':
            return { ...state, loading: false, error: action.payload };

        case 'CLEAR_CART':
            return initialState;

        default:
            return state;
    }
};