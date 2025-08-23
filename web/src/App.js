import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import ApiService from './services/ApiService';
import Login from './components/Login';
import Dashboard from './components/Dashboard';
import Orders from './components/Orders';
import Assets from './components/Assets';
import AdminPanel from './components/AdminPanel';
import AdminLayout from './components/AdminLayout';
import CustomerLayout from './components/CustomerLayout';
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
        <Routes>
          <Route
            path="/login"
            element={!user ? <Login onLogin={handleLogin} /> : <Navigate to={user.isAdmin ? "/admin" : "/dashboard"} />}
          />

          {/* Admin Routes */}
          <Route
            path="/admin"
            element={user && user.isAdmin ? (
              <AdminLayout user={user} onLogout={handleLogout}>
                <AdminPanel user={user} />
              </AdminLayout>
            ) : <Navigate to="/login" />}
          />
          <Route
            path="/admin/users"
            element={user && user.isAdmin ? (
              <AdminLayout user={user} onLogout={handleLogout}>
                <AdminPanel user={user} />
              </AdminLayout>
            ) : <Navigate to="/login" />}
          />

          {/* Customer Routes */}
          <Route
            path="/dashboard"
            element={user && !user.isAdmin ? (
              <CustomerLayout user={user} onLogout={handleLogout}>
                <Dashboard user={user} />
              </CustomerLayout>
            ) : user && user.isAdmin ? <Navigate to="/admin" /> : <Navigate to="/login" />}
          />
          <Route
            path="/orders"
            element={user && !user.isAdmin ? (
              <CustomerLayout user={user} onLogout={handleLogout}>
                <Orders user={user} />
              </CustomerLayout>
            ) : user && user.isAdmin ? <Navigate to="/admin" /> : <Navigate to="/login" />}
          />
          <Route
            path="/assets"
            element={user && !user.isAdmin ? (
              <CustomerLayout user={user} onLogout={handleLogout}>
                <Assets user={user} />
              </CustomerLayout>
            ) : user && user.isAdmin ? <Navigate to="/admin" /> : <Navigate to="/login" />}
          />
          <Route
            path="/portfolio"
            element={user && !user.isAdmin ? (
              <CustomerLayout user={user} onLogout={handleLogout}>
                <Dashboard user={user} />
              </CustomerLayout>
            ) : user && user.isAdmin ? <Navigate to="/admin" /> : <Navigate to="/login" />}
          />
          <Route
            path="/profile"
            element={user && !user.isAdmin ? (
              <CustomerLayout user={user} onLogout={handleLogout}>
                <Dashboard user={user} />
              </CustomerLayout>
            ) : user && user.isAdmin ? <Navigate to="/admin" /> : <Navigate to="/login" />}
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
          theme="dark"
        />
      </div>
    </Router>
  );
}

export default App;
