// src/store/reducers/ProductReducer.js

const initialState = {
    products: [],
    categories: [],        // ← danh sách phẳng (từ fetchCategories)
    categoryTree: [],      // ← cây danh mục (từ fetchRoot)
    pagination: {},
};

export const productReducer = (state = initialState, action) => {
    switch (action.type) {
        case "FETCH_PRODUCTS":
            return {
                ...state,
                products: action.payload.products,
                pagination: {
                    pageNumber: action.payload.pageNumber,
                    pageSize: action.payload.pageSize,
                    totalElements: action.payload.totalElements,
                    totalPages: action.payload.totalPages,
                    lastPage: action.payload.lastPage,
                },
            };

        case "FETCH_CATEGORIES":
            return {
                ...state,
                categories: action.payload, // danh sách phẳng
            };

        case "FETCH_CATEGORIES_TREE":
            return {
                ...state,
                categoryTree: action.payload, // cây danh mục (root + children)
            };

        default:
            return state;
    }
};