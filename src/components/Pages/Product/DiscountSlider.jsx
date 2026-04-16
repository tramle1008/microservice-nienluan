import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Swiper, SwiperSlide } from 'swiper/react';
import { Navigation, Pagination, Autoplay } from 'swiper/modules';
import 'swiper/css';
import 'swiper/css/navigation';
import 'swiper/css/pagination';

import { FaShoppingCart } from 'react-icons/fa';
import { Link } from 'react-router-dom';
import { truncateWords } from '../../../utils/truncate';
import ProductView from './ProductView'; // giữ nguyên component quick view
import { addToCart } from '../../../store/actions';
import { fetchDiscountedProducts } from '../../../store/actions';

const DiscountSlider = () => {
    const dispatch = useDispatch();
    const { products, loading, error } = useSelector(state => state.discountSlider);

    useEffect(() => {
        dispatch(fetchDiscountedProducts());
    }, [dispatch]);

    if (loading) {
        return (
            <div className="flex justify-center items-center py-16">
                <div className="animate-spin rounded-full h-12 w-12 border-t-4 border-red-600"></div>
            </div>
        );
    }

    if (error) {
        return <div className="text-center py-8 text-red-600">Lỗi: {error}</div>;
    }

    if (!products || products.length === 0) {
        return <div className="text-center py-8 text-gray-500">Không có sản phẩm giảm giá</div>;
    }

    const shouldLoop = products.length > 1;

    return (
        <section className="bg-[#92a695]">
            <div className="container mx-auto px-4 py-8">
                <h2 className="text-2xl md:text-3xl font-bold text-center mb-8 text-red-600">
                    SẢN PHẨM GIẢM GIÁ SỐC
                </h2>

                <Swiper
                    modules={[Navigation, Pagination, Autoplay]}
                    spaceBetween={20}
                    slidesPerView={1}
                    navigation
                    pagination={{ clickable: true }}
                    autoplay={{ delay: 3000, disableOnInteraction: false }}
                    loop={shouldLoop}
                    breakpoints={{
                        640: { slidesPerView: 2 },
                        768: { slidesPerView: 3 },
                        1024: { slidesPerView: 4 },
                    }}
                    className="pb-12"
                >
                    {products.map(product => (
                        <SwiperSlide key={product.productId}>
                            <DiscountProductCard product={product} />
                        </SwiperSlide>
                    ))}
                </Swiper>
            </div>
        </section>
    );
};

// Tách riêng card để tái sử dụng logic giống ProductCard
const DiscountProductCard = ({ product }) => {
    const dispatch = useDispatch();

    const [openProductView, setOpenProductView] = useState(false);
    const [selectedVariant, setSelectedVariant] = useState(null);
    const [forceMainImage, setForceMainImage] = useState(false);

    const { productId, productName, imageUrl, price, finalPrice, appliedDiscounts, variants, shortDescription } = product;

    const isAvailable = selectedVariant ? selectedVariant.stockQuantity > 0 : true;

    const handleProductView = () => setOpenProductView(true);

    const addToCartHandle = () => {
        const variantToAdd = selectedVariant || variants?.[0];
        if (!variantToAdd) return;
        dispatch(addToCart(variantToAdd.variantId, 1));
    };

    const handleVariantClick = (v) => {
        if (selectedVariant?.variantId === v.variantId) {
            setForceMainImage(prev => !prev);
        } else {
            setSelectedVariant(v);
            setForceMainImage(false);
        }
    };

    const displayImage = () => {
        if (!selectedVariant) return imageUrl;
        if (forceMainImage) return imageUrl;
        return selectedVariant.imageUrl && selectedVariant.imageUrl !== imageUrl
            ? selectedVariant.imageUrl
            : imageUrl;
    };

    const displayPrice = selectedVariant?.finalPrice ?? finalPrice ?? price;
    const discountPercent = appliedDiscounts?.[0]?.percentage ?? 0;

    return (
        <div className="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-xl transition-shadow duration-300 relative">
            {/* Badge giảm giá */}
            {discountPercent > 0 && (
                <div className="absolute top-3 left-3 z-10 bg-red-600 text-white text-xs font-bold px-2 py-1 rounded">
                    -{discountPercent}%
                </div>
            )}

            {/* Hình ảnh - click để mở quick view */}
            <div onClick={handleProductView} className="aspect-square overflow-hidden cursor-pointer">
                <img
                    src={displayImage()}
                    alt={productName}
                    className="w-full h-full object-cover transition-transform duration-300 hover:scale-105"
                />
            </div>

            {/* Nội dung */}
            <div className="p-4 flex flex-col gap-3">
                <div onClick={handleProductView} className="cursor-pointer">
                    <Link
                        to={`/product/${productId}`}
                        className="block text-lg font-semibold text-black line-clamp-2 hover:text-gray-600 transition-colors"
                    >
                        {truncateWords(productName, 5)}
                    </Link>
                </div>

                <p className="text-gray-400 text-sm line-clamp-2">
                    {truncateWords(shortDescription, 7)}
                </p>

                {/* Giá */}
                <div className="flex items-end gap-2">
                    {discountPercent > 0 && (
                        <div className="flex flex-col">
                            <span className="text-gray-400 line-through text-xs">
                                {Number(price).toLocaleString('vi-VN')}₫
                            </span>
                        </div>
                    )}
                    <span className={`font-bold ${discountPercent > 0 ? 'text-2xl text-red-600' : 'text-xl text-black'}`}>
                        {Number(displayPrice).toLocaleString('vi-VN')}₫
                    </span>
                </div>

                {/* Variant màu */}
                {variants?.length > 1 && (
                    <div className="flex gap-2 flex-wrap">
                        {variants.map(v => (
                            <button
                                key={v.variantId}
                                onClick={(e) => {
                                    e.stopPropagation();
                                    handleVariantClick(v);
                                }}
                                className={`px-3 py-1 text-xs border rounded transition-all ${selectedVariant?.variantId === v.variantId && !forceMainImage
                                    ? 'bg-emerald-700 text-white border-emerald-700'
                                    : 'bg-white text-gray-700 border-gray-300 hover:border-emerald-700'
                                    }`}
                            >
                                {v.color}
                            </button>
                        ))}
                    </div>
                )}

                {/* Nút thêm giỏ hàng */}
                <button
                    className={`py-2 px-4 rounded-lg w-full transition-colors duration-300 flex items-center justify-center gap-2 ${isAvailable
                        ? 'bg-emerald-700 hover:bg-emerald-900 text-white'
                        : 'bg-gray-300 text-gray-600 cursor-not-allowed'
                        }`}
                    disabled={!isAvailable}
                    onClick={addToCartHandle}
                >
                    <FaShoppingCart />
                    {isAvailable ? 'Thêm vào giỏ' : 'Hết hàng'}
                </button>
            </div>

            {/* Quick view modal */}
            <ProductView
                open={openProductView}
                setOpen={setOpenProductView}
                product={{ ...product, selectedVariant, forceMainImage, variants }}
                isAvailable={isAvailable}
            />
        </div>
    );
};

export default DiscountSlider;