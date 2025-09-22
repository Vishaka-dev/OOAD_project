import { useState, useMemo, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Separator } from "@/components/ui/separator";
import { Badge } from "@/components/ui/badge";
import { useStore } from "@/hooks/useStore";
import { useToast } from "@/hooks/use-toast";
import { Header } from "@/components/Header";
import { ShoppingCart, Trash2, Plus, Minus } from "lucide-react";

const Checkout = () => {
  const navigate = useNavigate();
  const {
    cart,
    getCartTotal,
    addOrder,
    clearCart,
    checkout,
    currentUser,
    updateCartQuantity,
    removeFromCart,
    forceSyncCart,
  } = useStore();
  const { toast } = useToast();

  const [deliveryAddress, setDeliveryAddress] = useState("");
  const [contactNumber, setContactNumber] = useState("");
  const [paymentMethod, setPaymentMethod] = useState<
    "credit_card" | "payhear" | "cod"
  >("credit_card");
  const [isPlacingOrder, setIsPlacingOrder] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  // Credit card fields
  const [cardNumber, setCardNumber] = useState("");
  const [cardHolderName, setCardHolderName] = useState("");
  const [expiryMonth, setExpiryMonth] = useState("");
  const [expiryYear, setExpiryYear] = useState("");
  const [cvv, setCvv] = useState("");

  // Load cart data on component mount
  useEffect(() => {
    const loadCart = async () => {
      try {
        await forceSyncCart();
      } catch (error) {
        console.error("Failed to load cart:", error);
      } finally {
        setIsLoading(false);
      }
    };
    loadCart();
  }, [forceSyncCart]);

  const totals = useMemo(() => {
    const subtotal = getCartTotal();
    const tax = subtotal * 0.08;
    const shipping = subtotal > 50 ? 0 : 5.99;
    const total = subtotal + tax + shipping;
    return { subtotal, tax, shipping, total };
  }, [getCartTotal, cart]);

  const handlePlaceOrder = async () => {
    if (cart.length === 0 || isPlacingOrder) return;

    if (!deliveryAddress || !contactNumber) {
      toast({
        title: "Missing Information",
        description: "Please fill in all required fields.",
        variant: "destructive",
      });
      return;
    }

    // Validate credit card fields if payment method is credit_card
    if (paymentMethod === "credit_card") {
      if (
        !cardNumber ||
        !cardHolderName ||
        !expiryMonth ||
        !expiryYear ||
        !cvv
      ) {
        toast({
          title: "Missing Credit Card Information",
          description: "Please fill in all credit card details.",
          variant: "destructive",
        });
        return;
      }
    }

    setIsPlacingOrder(true);
    try {
      const cardDetails =
        paymentMethod === "credit_card"
          ? {
              cardNumber,
              cardHolderName,
              expiryMonth,
              expiryYear,
              cvv,
            }
          : undefined;

      const result = await checkout(
        deliveryAddress,
        contactNumber,
        paymentMethod,
        cardDetails
      );

      toast({
        title: "Order Placed Successfully!",
        description: result,
      });

      navigate("/", { replace: true });
    } catch (error) {
      console.error("Checkout error:", error);
      let errorMessage =
        "There was an error placing your order. Please try again.";

      if (error instanceof Error) {
        errorMessage = error.message;
      } else if (error && typeof error === "object" && "message" in error) {
        errorMessage = String(error.message);
      } else if (typeof error === "string") {
        errorMessage = error;
      }

      toast({
        title: "Order Failed",
        description: errorMessage,
        variant: "destructive",
      });
    } finally {
      setIsPlacingOrder(false);
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-background">
        <Header
          onCartClick={() => navigate("/cart")}
          onSearchClick={() => {}}
        />
        <div className="mx-auto max-w-3xl px-4 py-10">
          <div className="animate-pulse">
            <div className="h-8 bg-muted rounded w-1/4 mb-6"></div>
            <div className="space-y-4">
              <div className="h-4 bg-muted rounded w-3/4"></div>
              <div className="h-4 bg-muted rounded w-1/2"></div>
              <div className="h-4 bg-muted rounded w-2/3"></div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (cart.length === 0) {
    return (
      <div className="min-h-screen bg-background">
        <Header
          onCartClick={() => navigate("/cart")}
          onSearchClick={() => {}}
        />
        <div className="mx-auto max-w-3xl px-4 py-10">
          <Card>
            <CardContent className="py-10 text-center">
              <ShoppingCart className="h-16 w-16 mx-auto mb-4 text-muted-foreground" />
              <h2 className="text-2xl font-bold mb-2">Your cart is empty</h2>
              <p className="mb-6 text-muted-foreground">
                Add some products to your cart to proceed with checkout.
              </p>
              <Button onClick={() => navigate("/products")}>
                Continue Shopping
              </Button>
            </CardContent>
          </Card>
        </div>
      </div>
    );
  }

  if (!currentUser) {
    return (
      <div className="min-h-screen bg-background">
        <Header
          onCartClick={() => navigate("/cart")}
          onSearchClick={() => {}}
        />
        <div className="mx-auto max-w-3xl px-4 py-10">
          <Card>
            <CardContent className="py-10 text-center">
              <h2 className="text-2xl font-bold mb-2">
                Please log in to continue
              </h2>
              <p className="mb-6 text-muted-foreground">
                You need to be logged in to place an order.
              </p>
              <Button onClick={() => navigate("/auth")}>Go to Login</Button>
            </CardContent>
          </Card>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background">
      <Header onCartClick={() => navigate("/cart")} onSearchClick={() => {}} />
      <div className="mx-auto max-w-5xl px-4 py-8">
        <div className="mb-6 flex items-center justify-between">
          <h1 className="text-2xl font-semibold">Checkout</h1>
          <Button variant="outline" onClick={() => navigate("/products")}>
            Continue Shopping
          </Button>
        </div>

        <div className="grid grid-cols-1 gap-6 md:grid-cols-3">
          <div className="md:col-span-2 space-y-6">
            <Card>
              <CardHeader>
                <CardTitle>Delivery Information</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div>
                  <label className="mb-2 block text-sm font-medium">
                    Delivery Address *
                  </label>
                  <Input
                    placeholder="123 Main St, City, State, ZIP"
                    value={deliveryAddress}
                    onChange={(e) => setDeliveryAddress(e.target.value)}
                    required
                  />
                </div>
                <div>
                  <label className="mb-2 block text-sm font-medium">
                    Contact Number *
                  </label>
                  <Input
                    type="tel"
                    placeholder="+1 (555) 123-4567"
                    value={contactNumber}
                    onChange={(e) => setContactNumber(e.target.value)}
                    required
                  />
                </div>
                <div>
                  <label className="mb-2 block text-sm font-medium">
                    Payment Method *
                  </label>
                  <select
                    className="w-full p-2 border border-gray-300 rounded-md"
                    value={paymentMethod}
                    onChange={(e) =>
                      setPaymentMethod(
                        e.target.value as "credit_card" | "payhear" | "cod"
                      )
                    }
                  >
                    <option value="credit_card">Credit Card</option>
                    <option value="payhear">PayHear</option>
                    <option value="cod">Cash on Delivery</option>
                  </select>
                </div>

                {/* Credit Card Fields - Only show when credit card is selected */}
                {paymentMethod === "credit_card" && (
                  <div className="space-y-4 p-4 border rounded-lg bg-muted/50">
                    <h4 className="font-medium">Credit Card Details</h4>
                    <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                      <div>
                        <label className="mb-2 block text-sm font-medium">
                          Card Number *
                        </label>
                        <Input
                          placeholder="1234 5678 9012 3456"
                          value={cardNumber}
                          onChange={(e) => setCardNumber(e.target.value)}
                          required
                        />
                      </div>
                      <div>
                        <label className="mb-2 block text-sm font-medium">
                          Card Holder Name *
                        </label>
                        <Input
                          placeholder="John Doe"
                          value={cardHolderName}
                          onChange={(e) => setCardHolderName(e.target.value)}
                          required
                        />
                      </div>
                    </div>
                    <div className="grid grid-cols-3 gap-4">
                      <div>
                        <label className="mb-2 block text-sm font-medium">
                          Expiry Month *
                        </label>
                        <select
                          className="w-full p-2 border border-gray-300 rounded-md"
                          value={expiryMonth}
                          onChange={(e) => setExpiryMonth(e.target.value)}
                          required
                        >
                          <option value="">Month</option>
                          {Array.from({ length: 12 }, (_, i) => (
                            <option
                              key={i + 1}
                              value={String(i + 1).padStart(2, "0")}
                            >
                              {String(i + 1).padStart(2, "0")}
                            </option>
                          ))}
                        </select>
                      </div>
                      <div>
                        <label className="mb-2 block text-sm font-medium">
                          Expiry Year *
                        </label>
                        <select
                          className="w-full p-2 border border-gray-300 rounded-md"
                          value={expiryYear}
                          onChange={(e) => setExpiryYear(e.target.value)}
                          required
                        >
                          <option value="">Year</option>
                          {Array.from({ length: 10 }, (_, i) => {
                            const year = new Date().getFullYear() + i;
                            return (
                              <option key={year} value={year}>
                                {year}
                              </option>
                            );
                          })}
                        </select>
                      </div>
                      <div>
                        <label className="mb-2 block text-sm font-medium">
                          CVV *
                        </label>
                        <Input
                          placeholder="123"
                          value={cvv}
                          onChange={(e) => setCvv(e.target.value)}
                          maxLength={4}
                          required
                        />
                      </div>
                    </div>
                  </div>
                )}
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Order items</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                {cart.map((item, index) => (
                  <div
                    key={`${item.id}-${item.backendId || index}`}
                    className="flex items-center justify-between p-3 border rounded-lg"
                  >
                    <div className="flex items-center gap-3 flex-1">
                      <div className="h-16 w-16 overflow-hidden rounded-md bg-muted">
                        <img
                          src={item.image}
                          alt={item.name}
                          className="h-full w-full object-cover"
                        />
                      </div>
                      <div className="flex-1">
                        <p className="font-medium">{item.name}</p>
                        <p className="text-sm text-muted-foreground">
                          {item.category} • {item.size}
                        </p>
                        <div className="flex items-center gap-2 mt-2">
                          <Badge variant="outline" className="text-xs">
                            ${item.price.toFixed(2)} each
                          </Badge>
                        </div>
                      </div>
                    </div>

                    <div className="flex items-center gap-3">
                      <div className="flex items-center gap-2">
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() =>
                            updateCartQuantity(item.id, item.quantity - 1)
                          }
                          disabled={item.quantity <= 1}
                        >
                          <Minus className="h-3 w-3" />
                        </Button>
                        <span className="w-8 text-center text-sm">
                          {item.quantity}
                        </span>
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() =>
                            updateCartQuantity(item.id, item.quantity + 1)
                          }
                          disabled={item.quantity >= item.stock}
                        >
                          <Plus className="h-3 w-3" />
                        </Button>
                      </div>

                      <div className="text-right min-w-[80px]">
                        <p className="font-semibold">
                          ${(item.price * item.quantity).toFixed(2)}
                        </p>
                      </div>

                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => removeFromCart(item.id)}
                        className="text-destructive hover:text-destructive"
                      >
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </div>
                  </div>
                ))}
              </CardContent>
            </Card>
          </div>

          <div className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle>Summary</CardTitle>
              </CardHeader>
              <CardContent className="space-y-3">
                <div className="flex justify-between text-sm">
                  <span>Subtotal</span>
                  <span>${totals.subtotal.toFixed(2)}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span>Tax</span>
                  <span>${totals.tax.toFixed(2)}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span>Shipping</span>
                  <span>
                    {totals.shipping === 0
                      ? "Free"
                      : `$${totals.shipping.toFixed(2)}`}
                  </span>
                </div>
                <Separator />
                <div className="flex justify-between font-semibold">
                  <span>Total</span>
                  <span>${totals.total.toFixed(2)}</span>
                </div>
                <Button
                  className="w-full"
                  onClick={handlePlaceOrder}
                  disabled={isPlacingOrder}
                >
                  {isPlacingOrder ? "Placing Order..." : "Place Order"}
                </Button>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Checkout;
