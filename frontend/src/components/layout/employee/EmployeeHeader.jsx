import React, { useState } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { FiLogOut, FiUser } from 'react-icons/fi';
import { logout } from '../../../store/authSlice.js';
import logo from '../../../assets/icons/logo.png';

const EmployeeHeader = () => {
    const [userMenuOpen, setUserMenuOpen] = useState(false);
    const dispatch = useDispatch();
    const navigate = useNavigate();
    const { user } = useSelector((state) => state.auth);

    const handleLogout = () => {
        dispatch(logout());
        navigate('/sign-in');
    };

    return (
        <header className="bg-white p-4 flex justify-between items-center border-b">
            <div className="flex items-center">
                <img className="h-10 ml-12" src={logo} alt="Logo" />
            </div>

            <div className="relative">
                <button onClick={() => setUserMenuOpen(!userMenuOpen)} className="flex items-center">
                    <FiUser className="w-6 h-6 rounded-full" />
                    <span className="ml-2 text-sm font-medium">{user?.name} {user?.surname}</span>
                </button>
                {userMenuOpen && (
                    <div className="absolute right-0 mt-2 w-48 bg-white rounded-md shadow-lg z-10">
                        <ul>
                            <li className="px-4 py-2 hover:bg-gray-100 cursor-pointer flex items-center">
                                <FiUser className="mr-2" /> Profile
                            </li>
                            <li onClick={handleLogout} className="px-4 py-2 hover:bg-gray-100 cursor-pointer flex items-center">
                                <FiLogOut className="mr-2" /> Logout
                            </li>
                        </ul>
                    </div>
                )}
            </div>
        </header>
    );
};

export default EmployeeHeader;
