
const initialState = {
    orders: [],
    totalPages: 0,
    loading: false,
    error: null,
};

const orderUserReducer = (state = initialState, action) => {
    switch (action.type) {
        case "ORDER_USER_REQUEST":
            return { ...state, loading: true, error: null };
        case "ORDER_USER_SUCCESS":
            return {
                ...state,
                loading: false,
                orders: action.payload.orders,
                totalPages: action.payload.totalPages,
            };
        case "ORDER_USER_FAILURE":
            return { ...state, loading: false, error: action.payload };
        default:
            return state;
    }
};

export default orderUserReducer;
