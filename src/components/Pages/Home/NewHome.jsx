
const NewHome = () => {
    const dispatch = useDispatch();
    const { products } = useSelector((state) => state.products);

    useEffect(() => {
        dispatch(fetchProduct());
    }, [dispatch]);
    //  4 sản phẩm đầu tiên 
    const featuredProducts = products.slice(2, 6);

    return (
        <>
            <div>
                <HeroBanner />
            </div>
            <section className="bg-[#f2f2f2] pt-5 pb-10 px-4 sm:px-8 lg:px-14">

                <div className="relative bg-cover bg-center h-[400px] rounded-xl overflow-hidden mb-16" >
                    <div className="absolute inset-0 bg-[#92a695] bg-opacity-50 flex items-center justify-center">
                        <div className="text-center text-white">
                            <h1 className="text-5xl font-[Montserrat] mb-4">Chào mừng đến với TileStyle</h1>
                            <p className="text-lg mb-6">Khám phá xu hướng thời trang mới nhất</p>
                            <Link to="/products" className="bg-white text-black px-6 py-2 rounded-full font-semibold hover:bg-gray-200">
                                Xem sản phẩm
                            </Link>
                        </div>
                    </div>
                </div>

                {/* Danh mục nổi bật */}
                <div className="mb-20">
                    <h2 className="text-2xl font-bold mb-8 text-center">Danh mục nổi bật</h2>
                    <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-6">
                        <a href="/products?categoryId=1">
                            <div className="bg-white p-4 rounded-lg shadow text-center cursor-pointer hover:shadow-lg transition">
                                <img
                                    src={`${import.meta.env.VITE_BACK_END_URL}/images/5f5b5285-eecc-4d8d-86c6-d588b2f82a46.png`}
                                    alt="quần áo"
                                    className="w-full h-40 object-cover rounded-md mb-2"
                                />
                                <p className="font-semibold">Quần Áo</p>
                            </div>
                        </a>
                        <a href="/products?categoryId=5">
                            <div className="bg-white p-4 rounded-lg shadow text-center">
                                <img
                                    src={`${import.meta.env.VITE_BACK_END_URL}/images/fdecb16b-94f0-4087-9190-f4368656b58a.jpg`}
                                    alt="nón"
                                    className="w-full h-40 object-cover rounded-md mb-2" />
                                <p className="font-semibold">Nón</p>
                            </div>
                        </a>
                        <a href="/products?categoryId=4">
                            <div className="bg-white p-4 rounded-lg shadow text-center">
                                <img src={`${import.meta.env.VITE_BACK_END_URL}/images/9b252faa-25a5-446c-86e2-2b2bfdb29d11.jpeg`} alt="Giày dép" className="w-full h-40 object-cover rounded-md mb-2" />
                                <p className="font-semibold">Giày dép</p>
                            </div>
                        </a>
                        <a href="/products?categoryId=8">
                            <div className="bg-white p-4 rounded-lg shadow text-center">
                                <img src={`${import.meta.env.VITE_BACK_END_URL}/images/7ef03459-a8ca-43e9-b01d-ea0f8e42d605.png`} alt="Phụ kiện" className="w-full h-40 object-cover rounded-md mb-2" />
                                <p className="font-semibold">Phụ kiện</p>
                            </div>
                        </a>
                    </div>
                </div>
                {/* Sản phẩm nổi bật */}
                <div>
                    <h2 className="text-2xl font-bold mb-8 text-center">Sản phẩm nổi bật</h2>
                    <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-8">
                        {featuredProducts.map((item) => (
                            <ProductCard key={item.productId} {...item} />
                        ))}
                    </div>
                    <div className="text-center mt-10">
                        <Link to="/products" className="inline-block bg-black text-white px-6 py-3 rounded-full hover:bg-gray-800">
                            Xem tất cả sản phẩm
                        </Link>
                    </div>
                </div>
            </section>
        </>
    );
};

export default NewHome;
