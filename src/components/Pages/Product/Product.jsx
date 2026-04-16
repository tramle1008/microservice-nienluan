import { FaExclamationTriangle } from "react-icons/fa";

import { useDispatch, useSelector } from "react-redux";
import { useEffect } from "react";
import { fetchCategories, fetchCategoryTreeProducts, fetchProduct } from "../../../store/actions";
import Filter from "../../Filter";
import useProductFilter from "../../useProductFilter";
import PaginationRounded from "../../PaginationRounded";
import Navbar from "../Navbar";
import ReusableFilter from "../../ReusableFilter";
import ProductCard from "./ProductCard";
import { useSearchParams } from "react-router-dom";

const Product = () => {
    const dispatch = useDispatch();
    const [searchParams] = useSearchParams();
    const categoryId = searchParams.get("categoryId");

    const { isLoading, errorMessage, products, pagination } = useSelector(
        (state) => state.products
    );

    // TỰ TẠO queryString – ĐƠN GIẢN, RÕ RÀNG
    useEffect(() => {
        const page = searchParams.get("page") || "1";
        const sortOrder = searchParams.get("sortOrder") || "asc";

        const params = new URLSearchParams();
        params.set("page", parseInt(page) - 1);
        params.set("size", "10");
        params.set("sort", `productId,${sortOrder}`);

        const queryString = params.toString();

        // GỌI API ĐÚNG
        if (categoryId) {
            const rootIds = [11, 12, 17, 18]; // danh sách root
            if (rootIds.includes(Number(categoryId))) {
                dispatch(fetchCategoryTreeProducts(categoryId, queryString));
            } else {
                dispatch(fetchProduct(categoryId, queryString));
            }
        } else {
            dispatch(fetchProduct(null, queryString));
        }
    }, [dispatch, searchParams]);
    useProductFilter();
    return (
        <section>
            <div className="lg:px-14 sm:px-8 px-2 py-4 2xl:w-[90%] 2xl:mx-auto bg-[#92a695] w-full">
                <ReusableFilter
                    filterParam="categoryId"
                    sortEnabled={true}
                />
                {isLoading ? (
                    <p>is loading ... </p>
                ) : errorMessage ? (
                    <div className='flex justify-center items-center h-[200px]'>
                        <FaExclamationTriangle className="text-slate-800 text-3xl mr-2" />
                        <span className="text-slate-800">{errorMessage}</span>
                    </div>
                ) : (

                    <section className="px-4 sm:px-4 lg:px-14 pt-10 2xl:w-[90%] 2xl:mx-auto">
                        <div className="grid grid-cols-1 sm:grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
                            {products.map((product) => (
                                <ProductCard key={product.productId} product={product} />
                            ))}
                        </div>
                        <div className="flex justify-center px-10 ">
                            <PaginationRounded
                                numberofPage={pagination?.totalPages}
                                totalProducts={pagination?.totalElements}
                            />

                        </div>
                    </section>

                )}
            </div>
        </section>
    );
};

export default Product;
