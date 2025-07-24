import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'sonner';

import AuthService from '../../services/authService';

const CompanySignUpPage = () => {
    const [companyName, setCompanyName] = useState('');
    const [name, setName] = useState('');
    const [surname, setSurname] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const navigate = useNavigate();

    const handleSignUp = async (e) => {
        e.preventDefault();
        try {
            await AuthService.companySignUp({ companyName, name, surname, email, password });
            toast.success('Company created successfully! Please check your email to verify your account.');
            navigate('/sign-in');
        } catch (error) {
            toast.error(error.response?.data?.message || 'Company creation failed');
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-100">
            <div className="max-w-md w-full bg-white p-8 rounded-lg shadow-md">
                <h2 className="text-2xl font-bold text-center mb-6">Create Company</h2>
                <form onSubmit={handleSignUp}>
                    <div className="mb-4">
                        <label className="block text-gray-700">Company Name</label>
                        <input
                            type="text"
                            className="w-full px-3 py-2 border rounded-lg"
                            value={companyName}
                            onChange={(e) => setCompanyName(e.target.value)}
                            required
                        />
                    </div>
                    <div className="mb-4">
                        <label className="block text-gray-700">Name</label>
                        <input
                            type="text"
                            className="w-full px-3 py-2 border rounded-lg"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                            required
                        />
                    </div>
                    <div className="mb-4">
                        <label className="block text-gray-700">Surname</label>
                        <input
                            type="text"
                            className="w-full px-3 py-2 border rounded-lg"
                            value={surname}
                            onChange={(e) => setSurname(e.target.value)}
                            required
                        />
                    </div>
                    <div className="mb-4">
                        <label className="block text-gray-700">Email</label>
                        <input
                            type="email"
                            className="w-full px-3 py-2 border rounded-lg"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                        />
                    </div>
                    <div className="mb-6">
                        <label className="block text-gray-700">Password</label>
                        <input
                            type="password"
                            className="w-full px-3 py-2 border rounded-lg"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                        />
                    </div>
                    <button type="submit" className="w-full bg-blue-500 text-white py-2 rounded-lg hover:bg-blue-600">
                        Create Company
                    </button>
                </form>
                <div className="text-center mt-4">
                    <p>Already have an account?{' '}
                        <Link to="/sign-in" className="text-blue-500 hover:underline">
                            Login
                        </Link>
                    </p>
                </div>
            </div>
        </div>
    );
};

export default CompanySignUpPage;
