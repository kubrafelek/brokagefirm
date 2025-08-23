import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Table, Button, Modal, Form, Alert, Badge } from 'react-bootstrap';
import { toast } from 'react-toastify';
import ApiService from '../services/ApiService';
import LoadingSpinner from './LoadingSpinner';

const Orders = ({ user }) => {
  const [orders, setOrders] = useState([]);
  const [assets, setAssets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [formData, setFormData] = useState({
    userId: user.userId,
    assetName: 'AAPL',
    side: 'BUY',
    size: '',
    price: ''
  });
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [ordersData, assetsData] = await Promise.all([
        ApiService.getOrders(),
        ApiService.getAssets()
      ]);
      setOrders(ordersData);
      setAssets(assetsData);
    } catch (error) {
      toast.error('Failed to load orders');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateOrder = async (e) => {
    e.preventDefault();
    setSubmitting(true);

    try {
      const orderData = {
        userId: parseInt(formData.userId),
        assetName: formData.assetName,
        side: formData.side,
        size: parseFloat(formData.size),
        price: parseFloat(formData.price)
      };

      await ApiService.createOrder(orderData);
      toast.success('Order created successfully!');
      setShowCreateModal(false);
      setFormData({
        userId: user.userId,
        assetName: 'AAPL',
        side: 'BUY',
        size: '',
        price: ''
      });
      fetchData();
    } catch (error) {
      toast.error(error.response?.data || 'Failed to create order');
    } finally {
      setSubmitting(false);
    }
  };

  const handleCancelOrder = async (orderId) => {
    if (!window.confirm('Are you sure you want to cancel this order?')) return;

    try {
      await ApiService.cancelOrder(orderId);
      toast.success('Order cancelled successfully!');
      fetchData();
    } catch (error) {
      toast.error(error.response?.data || 'Failed to cancel order');
    }
  };

  const getStatusBadgeClass = (status) => {
    switch (status) {
      case 'PENDING': return 'warning';
      case 'MATCHED': return 'success';
      case 'CANCELLED': return 'danger';
      default: return 'secondary';
    }
  };

  const getSideBadgeClass = (side) => {
    return side === 'BUY' ? 'success' : 'danger';
  };

  const canCancelOrder = (order) => {
    return order.status === 'PENDING' && (user.isAdmin || order.userId === user.userId);
  };

  if (loading) return <LoadingSpinner />;

  return (
    <Container className="mt-4">
      <Row>
        <Col>
          <div className="d-flex justify-content-between align-items-center mb-4">
            <h2>Orders</h2>
            <Button variant="primary" onClick={() => setShowCreateModal(true)}>
              Create New Order
            </Button>
          </div>
        </Col>
      </Row>

      <Row>
        <Col>
          <Card>
            <Card.Header>
              <h5 className="mb-0">Order History</h5>
            </Card.Header>
            <Card.Body>
              {orders.length === 0 ? (
                <Alert variant="info">No orders found. Create your first order!</Alert>
              ) : (
                <Table responsive hover>
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Asset</th>
                      <th>Side</th>
                      <th>Size</th>
                      <th>Price</th>
                      <th>Status</th>
                      <th>Created</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {orders.map((order) => (
                      <tr key={order.id}>
                        <td>#{order.id}</td>
                        <td><strong>{order.assetName}</strong></td>
                        <td>
                          <Badge bg={getSideBadgeClass(order.orderSide)}>
                            {order.orderSide}
                          </Badge>
                        </td>
                        <td>{parseFloat(order.size).toLocaleString()}</td>
                        <td>₺{parseFloat(order.price).toLocaleString()}</td>
                        <td>
                          <Badge bg={getStatusBadgeClass(order.status)}>
                            {order.status}
                          </Badge>
                        </td>
                        <td>
                          {order.createDate ? new Date(order.createDate).toLocaleDateString() : 'N/A'}
                        </td>
                        <td>
                          {canCancelOrder(order) && (
                            <Button
                              variant="outline-danger"
                              size="sm"
                              onClick={() => handleCancelOrder(order.id)}
                            >
                              Cancel
                            </Button>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </Table>
              )}
            </Card.Body>
          </Card>
        </Col>
      </Row>

      {/* Create Order Modal */}
      <Modal show={showCreateModal} onHide={() => setShowCreateModal(false)}>
        <Modal.Header closeButton>
          <Modal.Title>Create New Order</Modal.Title>
        </Modal.Header>
        <Form onSubmit={handleCreateOrder}>
          <Modal.Body>
            {user.isAdmin && (
              <Form.Group className="mb-3">
                <Form.Label>User ID</Form.Label>
                <Form.Control
                  type="number"
                  value={formData.userId}
                  onChange={(e) => setFormData({...formData, userId: e.target.value})}
                  required
                />
              </Form.Group>
            )}

            <Form.Group className="mb-3">
              <Form.Label>Asset</Form.Label>
              <Form.Select
                value={formData.assetName}
                onChange={(e) => setFormData({...formData, assetName: e.target.value})}
                required
              >
                <option value="AAPL">AAPL</option>
                <option value="GOOGL">GOOGL</option>
                <option value="MSFT">MSFT</option>
                <option value="NVDA">NVDA</option>
                <option value="TSLA">TSLA</option>
              </Form.Select>
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Order Side</Form.Label>
              <Form.Select
                value={formData.side}
                onChange={(e) => setFormData({...formData, side: e.target.value})}
                required
              >
                <option value="BUY">BUY</option>
                <option value="SELL">SELL</option>
              </Form.Select>
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Size</Form.Label>
              <Form.Control
                type="number"
                step="0.01"
                min="0.01"
                value={formData.size}
                onChange={(e) => setFormData({...formData, size: e.target.value})}
                placeholder="Enter quantity"
                required
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Price (₺)</Form.Label>
              <Form.Control
                type="number"
                step="0.01"
                min="0.01"
                value={formData.price}
                onChange={(e) => setFormData({...formData, price: e.target.value})}
                placeholder="Enter price per unit"
                required
              />
            </Form.Group>

            {/* Show available balance */}
            {formData.side === 'BUY' && (
              <Alert variant="info">
                Available TRY: ₺{(assets.find(a => a.assetName === 'TRY')?.usableSize || 0).toLocaleString()}
              </Alert>
            )}

            {formData.side === 'SELL' && (
              <Alert variant="info">
                Available {formData.assetName}: {(assets.find(a => a.assetName === formData.assetName)?.usableSize || 0).toLocaleString()}
              </Alert>
            )}
          </Modal.Body>
          <Modal.Footer>
            <Button variant="secondary" onClick={() => setShowCreateModal(false)}>
              Cancel
            </Button>
            <Button variant="primary" type="submit" disabled={submitting}>
              {submitting ? 'Creating...' : 'Create Order'}
            </Button>
          </Modal.Footer>
        </Form>
      </Modal>
    </Container>
  );
};

export default Orders;
