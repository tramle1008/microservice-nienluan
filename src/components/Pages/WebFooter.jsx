import { FaBuilding, FaFacebook, FaInstagramSquare, FaTiktok } from "react-icons/fa";
import { FaLocationPin, FaSquareXTwitter } from "react-icons/fa6";
import { Link } from "react-router-dom";
const WebFooter = () => {
    return (
        <footer className="bg-gray-900 text-white pt-12 pb-8">
            <div className="container mx-auto px-4">
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8 mb-8">

                    <div className="space-y-4">
                        <div className="flex items-center">
                            <FaBuilding />
                            <span className="ml-2 text-xl font-bold">Company</span>
                        </div>
                        <p className="text-gray-400">Liên hệ qua</p>
                        <div className="flex space-x-4">
                            <a href="#" className="text-gray-400 hover:text-white transition">
                                <FaFacebook />
                                <span className="sr-only">Facebook</span>
                            </a>
                            <a href="#" className="text-gray-400 hover:text-white transition">
                                <FaSquareXTwitter />
                                <span className="sr-only">Twitter</span>

                            </a>
                            <a href="#" className="text-gray-400 hover:text-white transition">
                                <FaInstagramSquare />
                                <span className="sr-only">Instagram</span>

                            </a>
                            <a href="#" className="text-gray-400 hover:text-white transition">
                                <FaTiktok />
                                <span className="sr-only">TikTok</span>
                            </a>
                        </div>
                    </div>

                    <div className="space-y-4">
                        <h3 className="text-lg font-semibold">Quick Links</h3>
                        <ul className="space-y-2">
                            <Link to="/" className="text-gray-400 hover:text-white transition">Home</Link><br />
                            <Link to="/products" className="text-gray-400 hover:text-white transition">Product</Link><br />
                            <Link to="/user/profile" className="text-gray-400 hover:text-white transition">Profile</Link>
                        </ul>
                    </div>

                    <div className="space-y-4">

                        <h3 className="text-lg font-semibold">Contact Us</h3>
                        <address className="not-italic text-gray-400">
                            <p>   <span className="flex items-center gap-x-2 hover:text-white transition">
                                <FaLocationPin /> 123 Nguyễn Văn Cừ, Ninh Kiều, Cần Thơ
                            </span> </p>
                            <p className="mt-2">Email: <a href="#" className="hover:text-white transition">info@company.com</a></p>
                            <p>Phone: <a href="#" className="hover:text-white transition">+84 123 456 7890</a></p>
                        </address>
                    </div>
                </div>

                <div className="border-t border-gray-800 pt-6 flex flex-col md:flex-row justify-between items-center">
                    <p className="text-gray-500 text-sm mb-4 md:mb-0">© 2025 Company. All rights reserved.</p>
                    <div className="flex space-x-6">
                        <a href="#" className="text-gray-500 hover:text-white text-sm transition">Privacy Policy</a>
                        <a href="#" className="text-gray-500 hover:text-white text-sm transition">Terms of Service</a>
                        <a href="#" className="text-gray-500 hover:text-white text-sm transition">Cookies</a>
                    </div>
                </div>
            </div>
        </footer>

    )
}
export default WebFooter;