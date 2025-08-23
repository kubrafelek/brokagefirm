import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import ApiService from './services/ApiService';
import Login from './components/Login';
import Dashboard from './components/Dashboard';
import Orders from './components/Orders';
import Assets from './components/Assets';
import AdminPanel from './components/AdminPanel';
import Navigation from './components/Navigation';
import LoadingSpinner from './components/LoadingSpinner';

function App() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check if user is already logged in
    const currentUser = ApiService.getCurrentUser();
    if (currentUser) {
      setUser(currentUser);
    }
    setLoading(false);
  }, []);

  const handleLogin = (userData) => {
    setUser(userData);
  };

  const handleLogout = () => {
    ApiService.logout();
    setUser(null);
  };

  if (loading) {
    return <LoadingSpinner />;
  }

  return (
    <Router>
      <div className="App">
        {user && <Navigation user={user} onLogout={handleLogout} />}

        <Routes>
          <Route
            path="/login"
            element={!user ? <Login onLogin={handleLogin} /> : <Navigate to={user.isAdmin ? "/admin" : "/dashboard"} />}
          />
          <Route
            path="/dashboard"
            element={user ? <Dashboard user={user} /> : <Navigate to="/login" />}
          />
          <Route
            path="/orders"
            element={user ? <Orders user={user} /> : <Navigate to="/login" />}
          />
          <Route
            path="/assets"
            element={user ? <Assets user={user} /> : <Navigate to="/login" />}
          />
          <Route
            path="/admin"
            element={user && user.isAdmin ? <AdminPanel user={user} /> : <Navigate to="/dashboard" />}
          />
          <Route
            path="/"
            element={<Navigate to={user ? (user.isAdmin ? "/admin" : "/dashboard") : "/login"} />}
          />
        </Routes>

        <ToastContainer
          position="top-right"
          autoClose={5000}
          hideProgressBar={false}
          newestOnTop={false}
          closeOnClick
          rtl={false}
          pauseOnFocusLoss
          draggable
          pauseOnHover
        />
      </div>
    </Router>
  );
}

export default App;
