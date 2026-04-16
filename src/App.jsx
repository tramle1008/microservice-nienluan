import { BrowserRouter, Routes, Route, useLocation } from 'react-router-dom';
import Home from './components/Pages/Home';
import Navbar from './components/Pages/Navbar';
import { Toaster } from 'react-hot-toast';
import Cart from './components/Pages/Cart/Cart';
import Login from './components/Pages/Auth/Login';
import Profile from './components/Pages/Auth/Profile';
import UpdateAccountForm from './components/UpdateAccountForm';
import Register from './components/Pages/Auth/Register';
import Checkout from './components/Pages/Order/Checkout';

import AdminRoute from './components/Pages/Admin/AdminRoute';
import HandleOrder from './components/Pages/Admin/HandleOrder';
import OrderShipped from './components/Pages/Admin/DetailOrder';
import AdminLayout from './components/Pages/Admin/AdminLayout';
import Logout from './components/Pages/Auth/Logout';
import OrderView from './components/Pages/Order/OrderView';
import DeliverRoute from './components/Pages/Delivery/DeliverRoute';
import Delivery from './components/Pages/Delivery/Delivery';
import AddProductForm from './components/Pages/Admin/AddProductForm';
import AdminProductList from './components/Pages/Admin/AdminProductList';

import DetailOrder from './components/Pages/Admin/DetailOrder';
import AddAdress from './components/Pages/Auth/AddAdress';
import ChangePasswd from './components/Pages/Auth/ChangePasswd';
import WebFooter from './components/Pages/WebFooter';
import Product from './components/Pages/Product/Product';
import ProductDetail from './components/Pages/Product/ProductDetail';
import SearchResults from './components/Pages/Product/SearchResults';
import DiscountSlider from './components/Pages/Product/DiscountSlider';
import SearchByImagePage from './components/Pages/Product/SearchByImagePage';
import AdminDiscountList from './components/Pages/Admin/Discount/AdminDiscountList';
import AdminDiscountAdd from './components/Pages/Admin/Discount/AdminDiscountAdd';
import AdminDiscountEdit from './components/Pages/Admin/Discount/AdminDiscountEdit';
import ViewOrderDetail from './components/Pages/Admin/ViewOrderAdmin/ViewOrderDetail';
import AllOrdersAdmin from './components/Pages/Admin/ViewOrderAdmin/AllOrdersAdmin';
import CategoriesProduct from './components/Pages/Product/CategoriesProduct';
import UpdateProductForm from './components/Pages/Admin/UpdateProductForm';
import FeeShipList from './components/Pages/Admin/FeeShip/FeeShipList';
import EditShippingFee from './components/Pages/Admin/FeeShip/EditShippingFee';

function AppContent() {
  const location = useLocation();
  const isAdminPage = location.pathname.startsWith('/admin');
  return (
    <>
      <Toaster position="top-center" />
      {!isAdminPage && <Navbar />}
      <Routes>

        <Route path="/" element={<Home />} />
        {/* <Route path="/products" element={<ProductList />} /> */}
        <Route path="/products/tree" element={<CategoriesProduct />} />

        <Route path="/product/:productId" element={<ProductDetail />} />
        <Route path="/products/public/search" element={<SearchResults />} />
        <Route path="/cart" element={<Cart />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/user/profile" element={<Profile />} />
        <Route path="/logout" element={<Logout />} />
        <Route path="/user/order" element={<OrderView />} />
        <Route path="/checkout" element={<Checkout />} />
        <Route path="/user/update/address" element={<AddAdress />} />
        <Route path="/user/update/password" element={<ChangePasswd />} />
        <Route path="/admin" element={<AdminRoute> <AdminLayout /></AdminRoute>} />
        <Route path="/admin/orders" element={<HandleOrder />} />
        <Route path="/admin/orders/ships" element={<OrderShipped />} />
        <Route path='/admin/product' element={<AdminProductList />} />
        <Route path="/admin/orders/all" element={<AllOrdersAdmin />} />
        <Route path="/admin/product/addproduct" element={<AddProductForm />} />
        <Route path="/deliver" element={<DeliverRoute> <Delivery /></DeliverRoute>} />
        <Route path="/admin/product/update/:productId" element={<UpdateProductForm />} />
        <Route path='/offers' element={<DiscountSlider />} />
        <Route path='/search-by-image' element={<SearchByImagePage />} />
        <Route path='/admin/discounts' element={<AdminDiscountList />} />
        <Route path='/admin/discounts/add' element={<AdminDiscountAdd />} />

        <Route path="/admin/shipping" element={<FeeShipList />} />

        <Route path="/admin/discount/edit/:id" element={<AdminDiscountEdit />} />
        <Route path="/admin/orders/detail/:orderId" element={<ViewOrderDetail />} />
        <Route path="/admin/shipping/edit/:provinceId" element={<EditShippingFee />} />
      </Routes>
      {!isAdminPage && <WebFooter />}

    </>
  );
}


function App() {
  return (
    <BrowserRouter>
      <AppContent />
    </BrowserRouter>
  );
}

export default App;