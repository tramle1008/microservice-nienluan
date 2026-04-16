// CategoriesProduct.jsx
import React, { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useSearchParams } from "react-router-dom";
import { CircularProgress, Alert, Typography } from "@mui/material";
import { fetchCategoryTreeProducts } from "../../../store/actions";
import ProductCard from "./ProductCard";
import PaginationRounded from "../../PaginationRounded";

const CategoriesProduct = () => {
    const [searchParams] = useSearchParams();
    const dispatch = useDispatch();

    const categoryId = searchParams.get("categoryId");
    const page = Number(searchParams.get("page") || 1);

    const { products = [], pagination = {}, isLoading = false, error = null } = useSelector(
        (state) => state.categoryTree || {}
    );

    const {
        totalPages = 1,
        totalElements = 0,
        pageNumber = 0,
        pageSize = 4,
        lastPage = true
    } = pagination;

    useEffect(() => {
        if (categoryId) {
            const qs = page > 1 ? `page=${page - 1}` : "";
            dispatch(fetchCategoryTreeProducts(categoryId, qs));
        }
    }, [categoryId, page, dispatch]);

    if (isLoading && products?.length === 0) {
        return <div className="flex justify-center py-20"><CircularProgress /></div>;
    }

    if (error) {
        return <Alert severity="error" className="m-10">Lỗi: {error}</Alert>;
    }

    if (!products || products.length === 0) {
        return <div className="text-center py-20 text-gray-500">Không có sản phẩm nào</div>;
    }

    return (
        <div className="w-full px-4 py-8 bg-[#92a695]"> {/* chỗ này nè */}
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
                {products.map((product) => (
                    <ProductCard key={product.productId} product={product} />
                ))}
            </div>
            {totalPages > 1 && <PaginationRounded numberofPage={totalPages} />}

            <div className="text-center mt-6 text-gray-600">
                Tổng: {totalElements} sản phẩm
            </div>
        </div>
    );
};

export default CategoriesProduct;