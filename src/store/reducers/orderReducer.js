const initialState = {
    loading: false,
    orders: [],
    error: null,
};

export const orderReducer = (state = initialState, action) => {
    switch (action.type) {
        case "ORDER_FETCH_REQUEST":
            return { ...state, loading: true, error: null };

        case "ORDER_FETCH_SUCCESS":
            return {
                ...state,
                loading: false,
                orders: action.payload.orders,
                totalPages: action.payload.totalPages,
                currentPage: action.payload.currentPage
            };

        case "ORDER_FETCH_FAIL":
            return { ...state, loading: false, error: action.payload };

        default:
            return state;
    }
};