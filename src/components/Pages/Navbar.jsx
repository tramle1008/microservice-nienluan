import {
    User, Heart, ShoppingCart, Menu, X, ChevronDown, Search
} from 'lucide-react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from "react-redux";
import { useEffect, useState } from "react";
import UserMenu from "../UserMenu";
import { fetchCart, fetchRoot } from "../../store/actions";
import { FaCamera } from 'react-icons/fa';

const Navbar = () => {
    const dispatch = useDispatch();
    const location = useLocation();
    const navigate = useNavigate();

    // Lấy dữ liệu từ Redux
    const { user } = useSelector(state => state.auth);
    const { products = [], categoryTree = [] } = useSelector(state => state.products);

    // Root categories = chính là categoryTree (vì API tree trả về root)
    const rootCategories = categoryTree;

    const flattenChildrenOnly = (nodes) => {
        let result = [];
        nodes.forEach(node => {
            // Bỏ qua root → chỉ duyệt children
            if (node.children && node.children.length > 0) {
                node.children.forEach(child => {
                    result.push({ categoryId: child.categoryId, categoryName: child.categoryName });
                    // Nếu child có con (cấp 3), tiếp tục duyệt
                    if (child.children && child.children.length > 0) {
                        result = result.concat(flattenChildrenOnly([child])); // đệ quy
                    }
                });
            }
        });
        return result;
    };

    const childCategoriesOnly = flattenChildrenOnly(categoryTree);
    const cartCount = products?.length || 0;

    // State
    const [isScrolled, setIsScrolled] = useState(false);
    const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
    const [searchQuery, setSearchQuery] = useState('');
    const [showCategories, setShowCategories] = useState(false);

    // Sticky khi scroll
    useEffect(() => {
        const handleScroll = () => setIsScrolled(window.scrollY > 20);
        window.addEventListener('scroll', handleScroll);
        return () => window.removeEventListener('scroll', handleScroll);
    }, []);

    // Fetch giỏ hàng
    useEffect(() => {
        if (user?.id) dispatch(fetchCart());
    }, [dispatch, user]);

    // Fetch cây danh mục
    useEffect(() => {
        dispatch(fetchRoot());
    }, [dispatch]);

    // Tìm kiếm
    const handleSearch = (e) => {
        e.preventDefault();
        const trimmed = searchQuery.trim();
        if (trimmed) {
            navigate(`/products/public/search?keyword=${encodeURIComponent(trimmed)}`);
        } else {
            navigate(`/products`);
        }
    };


    return (
        <nav className={`sticky top-0 z-50 transition-all duration-300 ${isScrolled ? 'bg-[#966993]/95 backdrop-blur-md shadow-md' : 'bg-[#966993]'
            }`}>
            {/* HEADER CHÍNH */}
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex items-center justify-between h-16 lg:h-20">

                    <div className="flex-shrink-0 pl-4 md:pl-6 lg:pl-8"> {/* càng lớn màn càng đẩy ra */}
                        <Link to="/" className="flex items-center space-x-3">
                            <img
                                src="/euphoria.webp"
                                alt="Euphoria Logo"
                                className="h-10 w-auto object-contain"
                            />
                            <span className="text-2xl font-bold text-white tracking-tight">
                                Euphoria
                            </span>
                        </Link>
                    </div>

                    {/* Thanh tìm kiếm (Desktop) */}
                    <div className="hidden lg:flex flex-1 max-w-2xl mx-8">
                        <form onSubmit={handleSearch} className="w-full">
                            <div className="relative">
                                <input
                                    type="text"
                                    value={searchQuery}
                                    onChange={(e) => setSearchQuery(e.target.value)}
                                    placeholder="Tìm kiếm sản phẩm, thương hiệu..."
                                    className="w-full px-5 py-3 pr-12 text-sm border border-gray-300 rounded-full focus:outline-none focus:border-blue-600 transition"
                                />
                                <button
                                    type="submit"
                                    className="absolute right-1 top-1/2 -translate-y-1/2 bg-blue-600 text-white p-2.5 rounded-full hover:bg-blue-700 transition"
                                >
                                    <Search className="w-5 h-5" />
                                </button>
                            </div>
                        </form>

                    </div>

                    {/* Desktop Menu */}
                    <div className="hidden lg:flex items-center space-x-4">
                        <Link
                            to="/search-by-image"  // sửa đúng route của bạn
                            className="relative text-white hover:text-blue-300 transition-transform duration-200 
                   hover:scale-110 group"
                            title="Tìm kiếm bằng hình ảnh"
                        >
                            <div className="relative">
                                <FaCamera className="w-6 h-6" />
                                {/* Hiệu ứng vòng tròn nền khi hover */}
                                <span className="absolute inset-0 -m-2 rounded-full bg-white/20 scale-0 
                             group-hover:scale-100 transition-transform duration-300" />
                            </div>
                        </Link>
                        {/* Nút Danh mục + Dropdown */}
                        <div className="relative">

                            <button
                                onClick={() => setShowCategories(prev => !prev)}
                                className="flex items-center text-white hover:text-blue-300 font-medium transition text-sm"
                            >
                                Danh mục
                                <ChevronDown className={`w-4 h-4 ml-0.5 transition-transform ${showCategories ? 'rotate-180' : ''}`} />
                            </button>

                            {/* Dropdown: TẤT CẢ danh mục ( + con) */}
                            {showCategories && (
                                <div className="absolute left-1/2 -translate-x-1/2 top-full mt-3 w-80 bg-white rounded-2xl shadow-xl border border-gray-200 overflow-hidden z-50
                    animate-in fade-in-0 slide-in-from-top-2 duration-200">
                                    <div className="py-3">
                                        {childCategoriesOnly.map((cat) => (
                                            <Link
                                                key={cat.categoryId}
                                                to={`/products/tree?categoryId=${cat.categoryId}`}
                                                onClick={() => setShowCategories(false)}
                                                className="block px-6 py-3.5 text-gray-700 hover:bg-blue-50 hover:text-emerald-600 
                               font-medium transition-all duration-200 hover:pl-8 border-l-4 border-transparent 
                               hover:border-l-blue-200"
                                            >
                                                {cat.categoryName}
                                            </Link>
                                        ))}
                                    </div>
                                </div>
                            )}
                        </div>

                        <Link to="/offers" className="text-white hover:text-blue-300 font-medium transition text-sm">
                            Ưu đãi
                        </Link>
                    </div>

                    {/* User + Cart */}
                    <div className="hidden lg:flex items-center space-x-6">

                        {user?.id ? (
                            <UserMenu />
                        ) : (
                            <Link
                                to="/login"
                                className="flex items-center px-4 py-1.5 bg-gradient-to-r from-indigo-700 to-red-500 font-bold rounded-md shadow-lg hover:from-indigo-500 hover:to-red-400 transition text-white text-sm"
                            >
                                Login
                            </Link>
                        )}

                        {/* <Link to="/wishlist" className="relative text-white hover:text-blue-300 transition">
                            <Heart className="w-5 h-5" />
                        </Link> */}

                        <Link to="/cart" className="relative text-white hover:text-blue-300 transition">
                            <ShoppingCart className="w-5 h-5" />
                            {cartCount > 0 && (
                                <span className="absolute -top-2 -right-2 bg-red-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center font-bold">
                                    {cartCount}
                                </span>
                            )}
                        </Link>
                    </div>

                    {/* Mobile menu button */}
                    <button
                        onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
                        className="lg:hidden p-2 text-white"
                    >
                        {isMobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
                    </button>
                </div>
            </div>

            {/* THANH ROOT - CÓ DROPDOWN */}
            <div className="hidden lg:flex justify-start items-center mt-3 pl-8 space-x-8">
                {rootCategories.length > 0 ? (
                    rootCategories.map((rootCat) => {
                        const hasChildren = rootCat.children && rootCat.children.length > 0;
                        const params = new URLSearchParams(location.search);
                        const currentCatId = params.get("categoryId");
                        const isActive = currentCatId && (
                            currentCatId == rootCat.categoryId ||
                            rootCat.children?.some(child => child.categoryId == currentCatId)
                        );

                        return (
                            <div key={rootCat.categoryId} className="relative group">

                                <div
                                    className={`
                            flex items-center gap-1 px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200 whitespace-nowrap
                            ${isActive
                                            ? "text-black bg-white shadow-md font-bold"
                                            : "text-white hover:text-black hover:bg-white/20"
                                        }
                        `}
                                >
                                    {rootCat.categoryName}
                                    {hasChildren && <ChevronDown className="w-4 h-4 transition-transform group-hover:rotate-180" />}
                                </div>

                                {/* Dropdown con */}
                                {hasChildren && (
                                    <div className="absolute  top-full mt-2 w-56 bg-white rounded-xl shadow-2xl border border-gray-100 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-300 z-50 origin-top-left">
                                        <div className="py-3">
                                            {rootCat.children.map((child) => {
                                                const isChildActive = currentCatId == child.categoryId;
                                                return (
                                                    <Link
                                                        key={child.categoryId}
                                                        to={`/products/tree?categoryId=${child.categoryId}`}
                                                        className={`
                                                block px-5 py-3 text-sm font-medium transition-all duration-200
                                                ${isChildActive
                                                                ? "text-emerald-600 bg-emerald-50 font-bold"
                                                                : "text-gray-700 hover:text-emerald-600 hover:bg-gray-50"
                                                            }
                                            `}
                                                    >
                                                        {child.categoryName}
                                                    </Link>
                                                );
                                            })}
                                        </div>
                                    </div>
                                )}
                            </div>
                        );
                    })
                ) : (
                    <p className="text-white/70 text-sm italic pl-8">Đang tải danh mục...</p>
                )}
            </div>

            {/* MOBILE MENU */}
            {isMobileMenuOpen && (
                <div className="lg:hidden bg-white border-t border-gray-200">
                    <div className="px-4 py-3 space-y-3">
                        <form onSubmit={handleSearch} className="mb-4">
                            <div className="relative">
                                <input
                                    type="text"
                                    value={searchQuery}
                                    onChange={(e) => setSearchQuery(e.target.value)}
                                    placeholder="Tìm kiếm..."
                                    className="w-full px-4 py-2 pr-10 border border-gray-300 rounded-lg focus:outline-none focus:border-blue-600"
                                />
                                <button type="submit" className="absolute right-2 top-1/2 -translate-y-1/2 text-blue-600">
                                    <Search className="w-5 h-5" />
                                </button>
                            </div>
                        </form>

                        <Link to="/" className="block py-2 text-gray-700  hover:text-gray-300 font-medium">Trang chủ</Link>
                        <div className="relative">
                            <button
                                onClick={() => setShowCategories(prev => !prev)}
                                className="flex items-center text-gray-700 hover:text-gray-300 font-medium transition text-sm"
                            >
                                Danh mục
                                <ChevronDown className={`w-4 h-4 ml-0.5 transition-transform ${showCategories ? 'rotate-180' : ''}`} />
                            </button>

                            {/* Dropdown: TẤT CẢ danh mục ( + con) */}
                            {showCategories && (
                                <div >
                                    {childCategoriesOnly.length > 0 ? (
                                        childCategoriesOnly.map((cat) => (
                                            <div key={cat.categoryId}>
                                                <button
                                                    onClick={() => {
                                                        navigate(`/products/tree?categoryId=${cat.categoryId}`);
                                                        setShowCategories(false);
                                                    }}
                                                    className="block w-full text-left font-semibold text-gray-900 
                                                              hover:text-[#518e53] hover:text-[1rem] hover:bg-gray-100
                                                              text-sm
                                                               py-2 px-1 
                                                                transition-all duration-200 
                                                               rounded-md"
                                                >
                                                    {cat.categoryName}
                                                </button>
                                            </div>
                                        ))
                                    ) : (
                                        <p className="col-span-full text-center text-gray-500 italic text-sm">Không có danh mục</p>
                                    )}
                                </div>
                            )}
                        </div>

                        <Link to="/offers" className="block py-2 text-gray-700 font-medium  hover:text-gray-300">Ưu đãi</Link>

                        <div className="border-t pt-3 mt-3 space-y-3">
                            {user?.id ? (
                                <UserMenu />
                            ) : (
                                <Link to="/login" className="block py-2 text-gray-700 font-medium  hover:text-gray-300">Đăng nhập</Link>
                            )}
                            {/* <Link to="/wishlist" className="flex items-center text-gray-700  hover:text-gray-300">
                                <Heart className="w-5 h-5 mr-2" /> Yêu thích
                            </Link> */}
                            <Link to="/cart" className="flex items-center text-gray-700 relative  hover:text-gray-300">
                                <ShoppingCart className="w-5 h-5 mr-2" /> Giỏ hàng
                                {cartCount > 0 && (
                                    <span className="absolute left-7 top-0 bg-red-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
                                        {cartCount}
                                    </span>
                                )}
                            </Link>
                        </div>
                    </div>
                </div>
            )}
        </nav>
    );
};

export default Navbar;