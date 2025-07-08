import React from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { Link, useNavigate } from 'react-router-dom';
import { logout } from '../store/authSlice';

const adminLinks = [
    { name: 'Dashboard', path: '/admin/dashboard' },
    { name: 'Employees', path: '/admin/employees' },
    { name: 'Roles', path: '/admin/roles' },
    { name: 'Evaluation Periods', path: '/admin/periods' },
    { name: 'Evaluation Templates', path: '/admin/templates' },
];

const employeeLinks = [
    { name: 'Dashboard', path: '/employee/dashboard' },
    { name: 'My Evaluations', path: '/employee/evaluations' },
];

const Sidebar = () => {
    const { user } = useSelector((state) => state.auth);
    const dispatch = useDispatch();
    const navigate = useNavigate();

    const handleLogout = () => {
        dispatch(logout());
        navigate('/sign-in');
    };

    const links = user?.roles?.includes('ADMIN') || user?.roles?.includes('COMPANY') ? adminLinks : employeeLinks;

    return (
        <div className="flex flex-col justify-between h-full p-4 bg-white shadow-md w-64">
            <div>
                <h1 className="text-2xl font-bold mb-8">Feedback 360</h1>
                <nav>
                    <ul>
                        {links.map((link) => (
                            <li key={link.name} className="mb-2">
                                <Link to={link.path} className="block p-2 rounded hover:bg-gray-200">
                                    {link.name}
                                </Link>
                            </li>
                        ))}
                    </ul>
                </nav>
            </div>
            <button 
                onClick={handleLogout}
                className="w-full py-2 text-white bg-red-600 rounded hover:bg-red-700"
            >
                Logout
            </button>
        </div>
    );
};

export default Sidebar;
