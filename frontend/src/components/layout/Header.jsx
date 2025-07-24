import React, { useState, useEffect } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { NavLink, useNavigate } from 'react-router-dom';
import { FiChevronDown, FiUser, FiLogOut } from 'react-icons/fi';
import { fetchPeriods, setSelectedPeriod } from '../../store/periodSlice';
import { logout } from '../../store/authSlice';

const Header = () => {
    const [dropdownOpen, setDropdownOpen] = useState(false);
    const [userMenuOpen, setUserMenuOpen] = useState(false);
    const dispatch = useDispatch();
    const navigate = useNavigate();
    const { periods, selectedPeriod, loading, error } = useSelector((state) => state.period);
    const { user } = useSelector((state) => state.auth);

    useEffect(() => {
        dispatch(fetchPeriods());
    }, [dispatch]);

    const handlePeriodSelect = (period) => {
        dispatch(setSelectedPeriod(period));
        setDropdownOpen(false);
    };

    const handleLogout = () => {
        dispatch(logout());
        navigate('/sign-in');
    };

    return (
        <header className="bg-white shadow-md p-4 flex justify-between items-center border-b">
            <div className="flex items-center">
                <div className="relative">
                    <button onClick={() => setDropdownOpen(!dropdownOpen)} className="h-10 flex items-center p-2 border rounded-md">
                        <span>{selectedPeriod ? selectedPeriod.periodName : 'Dönem Seçin'}</span>
                        <FiChevronDown className="ml-2" />
                    </button>
                    {dropdownOpen && (
                        <div className="absolute mt-2 w-64 bg-white rounded-md shadow-lg z-10">
                            {loading && <div className="px-4 py-2 text-gray-500">Yükleniyor...</div>}
                            {error && <div className="px-4 py-2 text-red-500">{error}</div>}
                            <ul>
                                {periods.map(period => (
                                    <li key={period.id} onClick={() => handlePeriodSelect(period)} className="px-4 py-2 hover:bg-gray-100 cursor-pointer">
                                        {period.periodName}
                                    </li>
                                ))}
                            </ul>
                        </div>
                    )}
                </div>
                <nav className="ml-6 space-x-4 hidden md:flex">
                    <NavLink to={`/dashboard/evaluations`} className="text-gray-600 hover:text-gray-900">Değerlendirmeler</NavLink>
                    <NavLink to={`/dashboard/evaluators`} className="text-gray-600 hover:text-gray-900">Kaynaklar</NavLink>
                    <NavLink to={`/dashboard/participants`} className="text-gray-600 hover:text-gray-900">Katılımcılar</NavLink>
                    <NavLink to={`/dashboard/competencies`} className="text-gray-600 hover:text-gray-900">Yetkinlikler</NavLink>
                    <NavLink to={`/dashboard/templates`} className="text-gray-600 hover:text-gray-900">Şablonlar</NavLink>
                </nav>
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

export default Header;
