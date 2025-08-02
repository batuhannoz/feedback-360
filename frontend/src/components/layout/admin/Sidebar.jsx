import React from 'react';
import { NavLink } from 'react-router-dom';
import { FiHome, FiUsers, FiBarChart2, FiSettings, FiClipboard, FiCheckSquare, FiStar, FiFileText } from 'react-icons/fi';
import logo from '../../../assets/icons/logo.png';

const Sidebar = () => {
    return (
        <div className="w-64 bg-white shadow-md">
            <div className="flex justify-center p-4 border-b">
                <img className="h-10" src={logo}/>
            </div>
            <nav className="mt-4">
                <h2 className="px-4 text-xs text-gray-500 uppercase tracking-wider">Genel</h2>
                <NavLink to="/dashboard" className="flex items-center px-4 py-2 mt-2 text-gray-700 hover:bg-gray-200" activeClassName="bg-gray-300">
                    <FiHome className="mr-2" /> Ana Sayfa
                </NavLink>
                <NavLink to="/dashboard/employees" className="flex items-center px-4 py-2 mt-2 text-gray-700 hover:bg-gray-200" activeClassName="bg-gray-300">
                    <FiUsers className="mr-2" /> Çalışanlar
                </NavLink>
                <NavLink to="/dashboard/settings" className="flex items-center px-4 py-2 mt-2 text-gray-700 hover:bg-gray-200" activeClassName="bg-gray-300">
                    <FiSettings className="mr-2" /> Ayarlar
                </NavLink>

                <h2 className="px-4 mt-6 text-xs text-gray-500 uppercase tracking-wider">360° Değerlendirme</h2>
                <NavLink to="/dashboard/evaluations" className="flex items-center px-4 py-2 mt-2 text-gray-700 hover:bg-gray-200" activeClassName="bg-gray-300">
                    <FiClipboard className="mr-2" /> Değerlendirmeler
                </NavLink>
                <NavLink to="/dashboard/participants" className="flex items-center px-4 py-2 mt-2 text-gray-700 hover:bg-gray-200" activeClassName="bg-gray-300">
                    <FiUsers className="mr-2" /> Katılımcılar
                </NavLink>
                <NavLink to="/dashboard/competencies" className="flex items-center px-4 py-2 mt-2 text-gray-700 hover:bg-gray-200" activeClassName="bg-gray-300">
                    <FiStar className="mr-2" /> Yetkinlikler
                </NavLink>
                <NavLink to="/dashboard/templates" className="flex items-center px-4 py-2 mt-2 text-gray-700 hover:bg-gray-200" activeClassName="bg-gray-300">
                    <FiFileText className="mr-2" /> Şablonlar
                </NavLink>
            </nav>
        </div>
    );
};

export default Sidebar;
