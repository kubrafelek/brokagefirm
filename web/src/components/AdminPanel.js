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
  const [activeTab, setActiveTab] = useState('create-orders');
  const [showMatchModal, setShowMatchModal] = useState(false);
  const [selectedOrder, setSelectedOrder] = useState(null);

  // Order creation state
  const [showCreateOrderModal, setShowCreateOrderModal] = useState(false);
  const [orderFormData, setOrderFormData] = useState({
    userId: '',
    assetName: 'AAPL',
    side: 'BUY',
    size: '',
    price: ''
  });
  const [submittingOrder, setSubmittingOrder] = useState(false);
  const [availableCustomers, setAvailableCustomers] = useState([]);
  const [availableAssets, setAvailableAssets] = useState([]);

  // Filter state for orders
  const [filterCustomer, setFilterCustomer] = useState('');
  const [filterAsset, setFilterAsset] = useState('');

  useEffect(() => {
    fetchAdminData();
    fetchAvailableAssets();
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

      // Extract unique customers from orders - this gives us real customer data
      const uniqueCustomers = [...new Set(ordersData.map(order => order.userId))];
      setCustomers(uniqueCustomers);

      // Set available customers for order creation from real data
      setAvailableCustomers(uniqueCustomers);
    } catch (error) {
      toast.error('Failed to load admin data');
    } finally {
      setLoading(false);
    }
  };

  const fetchAvailableAssets = async () => {
    try {
      const assets = await ApiService.getAvailableAssets();
      setAvailableAssets(assets);
    } catch (error) {
      console.error('Failed to fetch available assets:', error);
      // Fallback to default assets
      setAvailableAssets(['AAPL', 'GOOGL', 'MSFT', 'NVDA', 'TSLA']);
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

  const handleCreateOrder = async (e) => {
    e.preventDefault();
    setSubmittingOrder(true);

    try {
      const orderData = {
        userId: parseInt(orderFormData.userId),
        assetName: orderFormData.assetName,
        side: orderFormData.side,
        size: parseFloat(orderFormData.size),
        price: parseFloat(orderFormData.price)
      };

      await ApiService.createOrder(orderData);
      toast.success(`Order created successfully for Customer ${orderFormData.userId}!`);
      setShowCreateOrderModal(false);
      setOrderFormData({
        userId: '',
        assetName: availableAssets[0] || 'AAPL',
        side: 'BUY',
        size: '',
        price: ''
      });
      fetchAdminData(); // Refresh data
    } catch (error) {
      toast.error(error.response?.data || 'Failed to create order');
    } finally {
      setSubmittingOrder(false);
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

  // Filtered orders based on selected customer and asset
  const filteredOrders = allOrders.filter(order => {
    return (!filterCustomer || order.userId === filterCustomer) &&
           (!filterAsset || order.assetName === filterAsset);
  });

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
          <Card className="bg-secondary text-white">
            <Card.Body>
              <Card.Title>Matched Today</Card.Title>
              <h3>{allOrders.filter(o => o.status === 'MATCHED').length}</h3>
            </Card.Body>
          </Card>
        </Col>
      </Row>

      {/* Admin Tabs */}
      <Tabs activeKey={activeTab} onSelect={(k) => setActiveTab(k)} className="mb-3">
        <Tab eventKey="create-orders" title="Create Orders">
          <Card>
            <Card.Header>
              <div className="d-flex justify-content-between align-items-center">
                <h5 className="mb-0">Create New Order for Customer</h5>
                <Button
                  variant="primary"
                  onClick={() => setShowCreateOrderModal(true)}
                >
                  New Order
                </Button>
              </div>
            </Card.Header>
            <Card.Body>
              <Alert variant="info">
                <strong>Admin Order Creation:</strong> As an administrator, you can create orders for any customer.
                Make sure to verify customer balance before creating orders.
              </Alert>
            </Card.Body>
          </Card>
        </Tab>

        <Tab eventKey="orders" title="All Orders">
          <Card>
            <Card.Header>
              <div className="d-flex justify-content-between align-items-center">
                <h5 className="mb-0">All Orders Management</h5>
                <div className="d-flex gap-2">
                  <Form.Select
                    style={{ width: '150px' }}
                    value={filterCustomer}
                    onChange={(e) => setFilterCustomer(e.target.value)}
                  >
                    <option value="">All Customers</option>
                    {customers.map(customerId => (
                      <option key={customerId} value={customerId}>
                        Customer {customerId}
                      </option>
                    ))}
                  </Form.Select>
                  <Form.Select
                    style={{ width: '120px' }}
                    value={filterAsset}
                    onChange={(e) => setFilterAsset(e.target.value)}
                  >
                    <option value="">All Assets</option>
                    {availableAssets.map(asset => (
                      <option key={asset} value={asset}>
                        {asset}
                      </option>
                    ))}
                  </Form.Select>
                  <Button
                    variant="outline-secondary"
                    size="sm"
                    onClick={() => {
                      setFilterCustomer('');
                      setFilterAsset('');
                    }}
                  >
                    Clear Filters
                  </Button>
                </div>
              </div>
            </Card.Header>
            <Card.Body>
              {filteredOrders.length === 0 ? (
                <Alert variant="info">
                  {filterCustomer || filterAsset
                    ? "No orders found matching the selected filters"
                    : "No orders found"
                  }
                </Alert>
              ) : (
                <>
                  {(filterCustomer || filterAsset) && (
                    <Alert variant="info" className="mb-3">
                      <strong>Filters Applied:</strong>
                      {filterCustomer && ` Customer ${filterCustomer}`}
                      {filterCustomer && filterAsset && " • "}
                      {filterAsset && ` Asset ${filterAsset}`}
                      <span className="ms-2">({filteredOrders.length} order{filteredOrders.length !== 1 ? 's' : ''} found)</span>
                    </Alert>
                  )}
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
                      {filteredOrders.map((order) => (
                        <tr key={order.id}>
                          <td>#{order.id}</td>
                          <td>
                            <Badge bg="secondary" className="me-1">
                              {order.userId}
                            </Badge>
                          </td>
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
                </>
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
                <>
                  {/* TRY (Cash) Assets Table */}
                  <div className="mb-4">
                    <h6 className="text-success mb-3">
                      <i className="bi bi-cash-coin me-2"></i>
                      💰 Cash Holdings (TRY)
                    </h6>
                    {customerAssets.filter(asset => asset.assetName === 'TRY').length === 0 ? (
                      <Alert variant="info" className="mb-3">
                        No cash holdings found for Customer {selectedCustomer}
                      </Alert>
                    ) : (
                      <Table responsive striped className="mb-4">
                        <thead className="table-success">
                          <tr>
                            <th>Asset</th>
                            <th>Total Balance</th>
                            <th>Usable Balance</th>
                            <th>Reserved Amount</th>
                            <th>Status</th>
                          </tr>
                        </thead>
                        <tbody>
                          {customerAssets
                            .filter(asset => asset.assetName === 'TRY')
                            .map((asset, index) => {
                              const reserved = parseFloat(asset.size) - parseFloat(asset.usableSize);
                              return (
                                <tr key={index}>
                                  <td>
                                    <div className="d-flex align-items-center">
                                      <span className="me-2">💰</span>
                                      <strong className="text-success">{asset.assetName}</strong>
                                    </div>
                                  </td>
                                  <td className="fw-bold">
                                    ₺{parseFloat(asset.size).toLocaleString()}
                                  </td>
                                  <td className="text-success fw-bold">
                                    ₺{parseFloat(asset.usableSize).toLocaleString()}
                                  </td>
                                  <td className={reserved > 0 ? 'text-warning fw-bold' : 'text-muted'}>
                                    ₺{reserved.toLocaleString()}
                                  </td>
                                  <td>
                                    {reserved > 0 ? (
                                      <Badge bg="warning" text="dark">
                                        ₺{reserved.toLocaleString()} Reserved
                                      </Badge>
                                    ) : (
                                      <Badge bg="success">Fully Available</Badge>
                                    )}
                                  </td>
                                </tr>
                              );
                            })}
                        </tbody>
                      </Table>
                    )}
                  </div>

                  {/* Stock Assets Table */}
                  <div>
                    <h6 className="text-primary mb-3">
                      <i className="bi bi-graph-up me-2"></i>
                      📈 Stock Holdings
                    </h6>
                    {customerAssets.filter(asset => asset.assetName !== 'TRY').length === 0 ? (
                      <Alert variant="info">
                        No stock holdings found for Customer {selectedCustomer}
                      </Alert>
                    ) : (
                      <Table responsive striped>
                        <thead className="table-primary">
                          <tr>
                            <th>Stock Symbol</th>
                            <th>Total Shares</th>
                            <th>Usable Shares</th>
                            <th>Reserved Shares</th>
                            <th>Status</th>
                          </tr>
                        </thead>
                        <tbody>
                          {customerAssets
                            .filter(asset => asset.assetName !== 'TRY')
                            .map((asset, index) => {
                              const reserved = parseFloat(asset.size) - parseFloat(asset.usableSize);
                              const getStockIcon = (symbol) => {
                                const icons = {
                                  'AAPL': '🍎',
                                  'GOOGL': '🔍',
                                  'MSFT': '🖥️',
                                  'NVDA': '🎮',
                                  'TSLA': '🚗'
                                };
                                return icons[symbol] || '📈';
                              };

                              return (
                                <tr key={index}>
                                  <td>
                                    <div className="d-flex align-items-center">
                                      <span className="me-2">{getStockIcon(asset.assetName)}</span>
                                      <strong className="text-primary">{asset.assetName}</strong>
                                    </div>
                                  </td>
                                  <td className="fw-bold">
                                    {parseFloat(asset.size).toLocaleString()}
                                  </td>
                                  <td className="text-success fw-bold">
                                    {parseFloat(asset.usableSize).toLocaleString()}
                                  </td>
                                  <td className={reserved > 0 ? 'text-warning fw-bold' : 'text-muted'}>
                                    {reserved.toLocaleString()}
                                  </td>
                                  <td>
                                    {reserved > 0 ? (
                                      <Badge bg="warning" text="dark">
                                        {reserved.toLocaleString()} Reserved
                                      </Badge>
                                    ) : (
                                      <Badge bg="success">Fully Available</Badge>
                                    )}
                                  </td>
                                </tr>
                              );
                            })}
                        </tbody>
                      </Table>
                    )}
                  </div>

                  {/* Summary Card */}
                  <Card className="mt-4 bg-light">
                    <Card.Body>
                      <Row>
                        <Col md={4}>
                          <div className="text-center">
                            <h6 className="text-muted mb-1">Total Cash</h6>
                            <h5 className="text-success mb-0">
                              ₺{(customerAssets.find(a => a.assetName === 'TRY')?.size || 0).toLocaleString()}
                            </h5>
                          </div>
                        </Col>
                        <Col md={4}>
                          <div className="text-center">
                            <h6 className="text-muted mb-1">Available Cash</h6>
                            <h5 className="text-success mb-0">
                              ₺{(customerAssets.find(a => a.assetName === 'TRY')?.usableSize || 0).toLocaleString()}
                            </h5>
                          </div>
                        </Col>
                        <Col md={4}>
                          <div className="text-center">
                            <h6 className="text-muted mb-1">Stock Holdings</h6>
                            <h5 className="text-primary mb-0">
                              {customerAssets.filter(a => a.assetName !== 'TRY').length} Stocks
                            </h5>
                          </div>
                        </Col>
                      </Row>
                    </Card.Body>
                  </Card>
                </>
              )}
            </Card.Body>
          </Card>
        </Tab>
      </Tabs>

      {/* Create Order Modal */}
      <Modal show={showCreateOrderModal} onHide={() => setShowCreateOrderModal(false)} size="lg">
        <Modal.Header closeButton>
          <Modal.Title>Create New Order for Customer</Modal.Title>
        </Modal.Header>
        <Form onSubmit={handleCreateOrder}>
          <Modal.Body>
            <Row>
              <Col md={6}>
                <Form.Group controlId="formUserId" className="mb-3">
                  <Form.Label>Customer ID</Form.Label>
                  <Form.Select
                    value={orderFormData.userId}
                    onChange={(e) => setOrderFormData({ ...orderFormData, userId: e.target.value })}
                    required
                  >
                    <option value="">Select Customer</option>
                    {availableCustomers.map(customerId => (
                      <option key={customerId} value={customerId}>
                        Customer {customerId}
                      </option>
                    ))}
                  </Form.Select>
                  <Form.Text className="text-muted">
                    Select the customer for whom you want to create this order
                  </Form.Text>
                </Form.Group>

                <Form.Group controlId="formAssetName" className="mb-3">
                  <Form.Label>Asset</Form.Label>
                  <Form.Select
                    value={orderFormData.assetName}
                    onChange={(e) => setOrderFormData({ ...orderFormData, assetName: e.target.value })}
                    required
                  >
                    {availableAssets.map(asset => (
                      <option key={asset} value={asset}>
                        {asset}
                      </option>
                    ))}
                  </Form.Select>
                  <Form.Text className="text-muted">
                    Choose the asset to trade
                  </Form.Text>
                </Form.Group>

                <Form.Group controlId="formOrderSide" className="mb-3">
                  <Form.Label>Order Side</Form.Label>
                  <Form.Select
                    value={orderFormData.side}
                    onChange={(e) => setOrderFormData({ ...orderFormData, side: e.target.value })}
                    required
                  >
                    <option value="BUY">BUY</option>
                    <option value="SELL">SELL</option>
                  </Form.Select>
                  <Form.Text className="text-muted">
                    Select whether this is a buy or sell order
                  </Form.Text>
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group controlId="formSize" className="mb-3">
                  <Form.Label>Size</Form.Label>
                  <Form.Control
                    type="number"
                    step="0.01"
                    min="0.01"
                    value={orderFormData.size}
                    onChange={(e) => setOrderFormData({ ...orderFormData, size: e.target.value })}
                    placeholder="Enter quantity"
                    required
                  />
                  <Form.Text className="text-muted">
                    Enter the quantity of assets to trade
                  </Form.Text>
                </Form.Group>

                <Form.Group controlId="formPrice" className="mb-3">
                  <Form.Label>Price (₺)</Form.Label>
                  <Form.Control
                    type="number"
                    step="0.01"
                    min="0.01"
                    value={orderFormData.price}
                    onChange={(e) => setOrderFormData({ ...orderFormData, price: e.target.value })}
                    placeholder="Enter price per unit"
                    required
                  />
                  <Form.Text className="text-muted">
                    Enter the price per unit in Turkish Lira
                  </Form.Text>
                </Form.Group>

                {orderFormData.size && orderFormData.price && (
                  <Alert variant="info">
                    <strong>Total Order Value:</strong> ₺{(parseFloat(orderFormData.size || 0) * parseFloat(orderFormData.price || 0)).toLocaleString()}
                  </Alert>
                )}
              </Col>
            </Row>

            <Alert variant="warning">
              <strong>Admin Reminder:</strong> Please verify that the customer has sufficient balance before creating the order.
              You can check customer assets in the "Customer Assets" tab.
            </Alert>
          </Modal.Body>
          <Modal.Footer>
            <Button variant="secondary" onClick={() => setShowCreateOrderModal(false)}>
              Cancel
            </Button>
            <Button variant="primary" type="submit" disabled={submittingOrder}>
              {submittingOrder ? 'Creating Order...' : 'Create Order'}
            </Button>
          </Modal.Footer>
        </Form>
      </Modal>

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
