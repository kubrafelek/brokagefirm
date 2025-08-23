import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Table, Button, Modal, Form, Alert, Badge, Tabs, Tab } from 'react-bootstrap';
import { toast } from 'react-toastify';
import ApiService from '../services/ApiService';
import LoadingSpinner from './LoadingSpinner';

const AdminPanel = ({ user }) => {
  const [allOrders, setAllOrders] = useState([]);
  const [pendingOrders, setPendingOrders] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [selectedCustomer, setSelectedCustomer] = useState('');
  const [customerAssets, setCustomerAssets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('orders');
  const [showMatchModal, setShowMatchModal] = useState(false);
  const [selectedOrder, setSelectedOrder] = useState(null);

  useEffect(() => {
    fetchAdminData();
  }, []);

  const fetchAdminData = async () => {
    try {
      setLoading(true);
      const [ordersData, pendingData] = await Promise.all([
        ApiService.getOrders(),
        ApiService.getPendingOrders()
      ]);

      setAllOrders(ordersData);
      setPendingOrders(pendingData);

      // Extract unique customers from orders
      const uniqueCustomers = [...new Set(ordersData.map(order => order.userId))];
      setCustomers(uniqueCustomers);
    } catch (error) {
      toast.error('Failed to load admin data');
    } finally {
      setLoading(false);
    }
  };

  const fetchCustomerAssets = async (customerId) => {
    try {
      const assets = await ApiService.getAssets(customerId);
      setCustomerAssets(assets);
    } catch (error) {
      toast.error('Failed to load customer assets');
    }
  };

  const handleCustomerChange = (customerId) => {
    setSelectedCustomer(customerId);
    if (customerId) {
      fetchCustomerAssets(customerId);
    } else {
      setCustomerAssets([]);
    }
  };

  const handleMatchOrder = async () => {
    if (!selectedOrder) return;

    try {
      await ApiService.matchOrder(selectedOrder.id);
      toast.success('Order matched successfully!');
      setShowMatchModal(false);
      setSelectedOrder(null);
      fetchAdminData();
    } catch (error) {
      toast.error(error.response?.data || 'Failed to match order');
    }
  };

  const handleCancelOrder = async (orderId) => {
    if (!window.confirm('Are you sure you want to cancel this order?')) return;

    try {
      await ApiService.cancelOrder(orderId);
      toast.success('Order cancelled successfully!');
      fetchAdminData();
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

  if (loading) return <LoadingSpinner />;

  return (
    <Container className="mt-4">
      <Row>
        <Col>
          <h2>Admin Panel</h2>
          <p className="text-muted">Administrative tools and oversight</p>
        </Col>
      </Row>

      {/* Admin Stats */}
      <Row className="mb-4">
        <Col md={3}>
          <Card className="bg-primary text-white">
            <Card.Body>
              <Card.Title>Total Orders</Card.Title>
              <h3>{allOrders.length}</h3>
            </Card.Body>
          </Card>
        </Col>
        <Col md={3}>
          <Card className="bg-warning text-dark">
            <Card.Body>
              <Card.Title>Pending Orders</Card.Title>
              <h3>{pendingOrders.length}</h3>
            </Card.Body>
          </Card>
        </Col>
        <Col md={3}>
          <Card className="bg-success text-white">
            <Card.Body>
              <Card.Title>Active Customers</Card.Title>
              <h3>{customers.length}</h3>
            </Card.Body>
          </Card>
        </Col>
        <Col md={3}>
          <Card className="bg-info text-white">
            <Card.Body>
              <Card.Title>Matched Today</Card.Title>
              <h3>{allOrders.filter(o => o.status === 'MATCHED').length}</h3>
            </Card.Body>
          </Card>
        </Col>
      </Row>

      {/* Admin Tabs */}
      <Tabs activeKey={activeTab} onSelect={(k) => setActiveTab(k)} className="mb-3">
        <Tab eventKey="orders" title="All Orders">
          <Card>
            <Card.Header>
              <h5 className="mb-0">All Orders Management</h5>
            </Card.Header>
            <Card.Body>
              {allOrders.length === 0 ? (
                <Alert variant="info">No orders found</Alert>
              ) : (
                <Table responsive hover>
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Customer ID</th>
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
                    {allOrders.map((order) => (
                      <tr key={order.id}>
                        <td>#{order.id}</td>
                        <td>{order.userId}</td>
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
                          <div className="d-flex gap-1">
                            {order.status === 'PENDING' && (
                              <>
                                <Button
                                  variant="outline-success"
                                  size="sm"
                                  onClick={() => {
                                    setSelectedOrder(order);
                                    setShowMatchModal(true);
                                  }}
                                >
                                  Match
                                </Button>
                                <Button
                                  variant="outline-danger"
                                  size="sm"
                                  onClick={() => handleCancelOrder(order.id)}
                                >
                                  Cancel
                                </Button>
                              </>
                            )}
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </Table>
              )}
            </Card.Body>
          </Card>
        </Tab>

        <Tab eventKey="pending" title="Pending Orders">
          <Card>
            <Card.Header>
              <h5 className="mb-0">Pending Orders Queue</h5>
            </Card.Header>
            <Card.Body>
              {pendingOrders.length === 0 ? (
                <Alert variant="success">No pending orders to process</Alert>
              ) : (
                <Table responsive hover>
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Customer ID</th>
                      <th>Asset</th>
                      <th>Side</th>
                      <th>Size</th>
                      <th>Price</th>
                      <th>Total Value</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {pendingOrders.map((order) => (
                      <tr key={order.id}>
                        <td>#{order.id}</td>
                        <td>{order.userId}</td>
                        <td><strong>{order.assetName}</strong></td>
                        <td>
                          <Badge bg={getSideBadgeClass(order.orderSide)}>
                            {order.orderSide}
                          </Badge>
                        </td>
                        <td>{parseFloat(order.size).toLocaleString()}</td>
                        <td>₺{parseFloat(order.price).toLocaleString()}</td>
                        <td>₺{(parseFloat(order.size) * parseFloat(order.price)).toLocaleString()}</td>
                        <td>
                          <div className="d-flex gap-1">
                            <Button
                              variant="success"
                              size="sm"
                              onClick={() => {
                                setSelectedOrder(order);
                                setShowMatchModal(true);
                              }}
                            >
                              Match
                            </Button>
                            <Button
                              variant="outline-danger"
                              size="sm"
                              onClick={() => handleCancelOrder(order.id)}
                            >
                              Cancel
                            </Button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </Table>
              )}
            </Card.Body>
          </Card>
        </Tab>

        <Tab eventKey="customers" title="Customer Assets">
          <Card>
            <Card.Header>
              <div className="d-flex justify-content-between align-items-center">
                <h5 className="mb-0">Customer Asset Management</h5>
                <Form.Select
                  style={{ width: '200px' }}
                  value={selectedCustomer}
                  onChange={(e) => handleCustomerChange(e.target.value)}
                >
                  <option value="">Select Customer</option>
                  {customers.map(customerId => (
                    <option key={customerId} value={customerId}>
                      Customer {customerId}
                    </option>
                  ))}
                </Form.Select>
              </div>
            </Card.Header>
            <Card.Body>
              {!selectedCustomer ? (
                <Alert variant="info">Select a customer to view their assets</Alert>
              ) : customerAssets.length === 0 ? (
                <Alert variant="warning">No assets found for selected customer</Alert>
              ) : (
                <Table responsive>
                  <thead>
                    <tr>
                      <th>Asset</th>
                      <th>Total Size</th>
                      <th>Usable Size</th>
                      <th>Reserved</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {customerAssets.map((asset, index) => {
                      const reserved = parseFloat(asset.size) - parseFloat(asset.usableSize);
                      return (
                        <tr key={index}>
                          <td><strong>{asset.assetName}</strong></td>
                          <td>
                            {parseFloat(asset.size).toLocaleString()}
                            {asset.assetName === 'TRY' && ' ₺'}
                          </td>
                          <td className="text-success">
                            {parseFloat(asset.usableSize).toLocaleString()}
                            {asset.assetName === 'TRY' && ' ₺'}
                          </td>
                          <td className={reserved > 0 ? 'text-warning' : 'text-muted'}>
                            {reserved.toLocaleString()}
                            {asset.assetName === 'TRY' && ' ₺'}
                          </td>
                          <td>
                            {reserved > 0 ? (
                              <Badge bg="warning" text="dark">Reserved</Badge>
                            ) : (
                              <Badge bg="success">Available</Badge>
                            )}
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </Table>
              )}
            </Card.Body>
          </Card>
        </Tab>
      </Tabs>

      {/* Match Order Modal */}
      <Modal show={showMatchModal} onHide={() => setShowMatchModal(false)}>
        <Modal.Header closeButton>
          <Modal.Title>Match Order</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {selectedOrder && (
            <div>
              <p><strong>Order ID:</strong> #{selectedOrder.id}</p>
              <p><strong>Customer:</strong> {selectedOrder.userId}</p>
              <p><strong>Asset:</strong> {selectedOrder.assetName}</p>
              <p><strong>Side:</strong> <Badge bg={getSideBadgeClass(selectedOrder.orderSide)}>{selectedOrder.orderSide}</Badge></p>
              <p><strong>Size:</strong> {parseFloat(selectedOrder.size).toLocaleString()}</p>
              <p><strong>Price:</strong> ₺{parseFloat(selectedOrder.price).toLocaleString()}</p>
              <p><strong>Total Value:</strong> ₺{(parseFloat(selectedOrder.size) * parseFloat(selectedOrder.price)).toLocaleString()}</p>
              <Alert variant="warning">
                Are you sure you want to match this order? This action cannot be undone.
              </Alert>
            </div>
          )}
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowMatchModal(false)}>
            Cancel
          </Button>
          <Button variant="success" onClick={handleMatchOrder}>
            Confirm Match
          </Button>
        </Modal.Footer>
      </Modal>
    </Container>
  );
};

export default AdminPanel;
