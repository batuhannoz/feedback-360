import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { toast } from 'sonner';

import AuthService from '../../services/authService';

const AcceptInvitationPage = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();

    const [name, setName] = useState('');
    const [surname, setSurname] = useState('');
    const [password, setPassword] = useState('');
    const [email, setEmail] = useState('');
    const [token, setToken] = useState('');

    useEffect(() => {
        const emailFromQuery = searchParams.get('email');
        const tokenFromQuery = searchParams.get('token');

        if (emailFromQuery && tokenFromQuery) {
            setEmail(emailFromQuery);
            setToken(tokenFromQuery);
        } else {
            toast.error('Invalid invitation link');
            navigate('/sign-in');
        }
    }, [searchParams, navigate]);

    const handleAcceptInvitation = async (e) => {
        e.preventDefault();
        try {
            await AuthService.completeInvitation({ name, surname, email, password, token });
            toast.success('Invitation accepted successfully! You can now log in.');
            navigate('/sign-in');
        } catch (error) {
            toast.error(error.response?.data?.message || 'Failed to accept invitation');
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-100">
            <div className="max-w-md w-full bg-white p-8 rounded-lg shadow-md">
                <h2 className="text-2xl font-bold text-center mb-6">Accept Invitation</h2>
                <form onSubmit={handleAcceptInvitation}>
                    <div className="mb-4">
                        <label className="block text-gray-700">Email</label>
                        <input
                            type="email"
                            className="w-full px-3 py-2 border rounded-lg bg-gray-200"
                            value={email}
                            readOnly
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
                        Complete Registration
                    </button>
                </form>
            </div>
        </div>
    );
};

export default AcceptInvitationPage;
