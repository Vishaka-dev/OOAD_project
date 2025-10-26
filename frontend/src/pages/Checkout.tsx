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
import { ShoppingCart, Trash2, Plus, Minus, X, RotateCcw } from "lucide-react";
import { calculateExtraCost } from "@/lib/personalization-utils";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog";

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
    resetCartCompletely,
    forceClearCartState,
  } = useStore();
  const { toast } = useToast();

  const [deliveryAddress, setDeliveryAddress] = useState("");
  const [contactNumber, setContactNumber] = useState("");
  const [paymentMethod, setPaymentMethod] = useState<
    "credit_card" | "payhear" | "cod"
  >("credit_card");
  const [isPlacingOrder, setIsPlacingOrder] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [cartKey, setCartKey] = useState(0); // Force re-render when cart changes
  const [justCompletedCheckout, setJustCompletedCheckout] = useState(false); // Track if we just completed checkout

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
        // Only sync cart if user is authenticated and cart is empty
        // But don't sync if we just completed checkout or cleared the cart
        if (
          currentUser &&
          cart.length === 0 &&
          !justCompletedCheckout &&
          cartKey === 0
        ) {
          console.log("🔄 Loading cart from backend...");
          await forceSyncCart();
        } else if (justCompletedCheckout || cartKey > 0) {
          console.log(
            "🔄 Skipping cart sync - cart was recently cleared or checkout completed"
          );
        }
      } catch (error) {
        console.error("Failed to load cart:", error);
      } finally {
        setIsLoading(false);
      }
    };
    loadCart();
  }, [forceSyncCart, currentUser, cart.length, cartKey, justCompletedCheckout]);

  // Update cart key when cart changes to force re-render
  useEffect(() => {
    setCartKey((prev) => prev + 1);
  }, [cart.length]);

  // Force clear cart if we just completed checkout and cart has items
  useEffect(() => {
    if (justCompletedCheckout && cart.length > 0) {
      console.log(
        "🔄 Force clearing cart - checkout completed but cart still has items"
      );
      forceClearCartState();
    }
  }, [justCompletedCheckout, cart.length, forceClearCartState]);

  // Helper function to get current extra price based on latest pricing logic
  const getCurrentExtraPrice = (item: any) => {
    if (
      item.personalizationDetails &&
      Object.keys(item.personalizationDetails).length > 0
    ) {
      return calculateExtraCost(item.personalizationDetails);
    }
    return 0;
  };

  const totals = useMemo(() => {
    // Calculate subtotal with current personalization prices
    const subtotal = cart.reduce((total, item) => {
      const basePrice = item.price * item.quantity;
      const extraPrice = getCurrentExtraPrice(item) * item.quantity;
      return total + basePrice + extraPrice;
    }, 0);

    const shipping = subtotal > 5000 ? 0 : 0; // Free shipping
    const total = subtotal + shipping;
    return { subtotal, tax: 0, shipping, total };
  }, [cart]);

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

    // Validate stock before placing order
    const outOfStockItems = cart.filter(
      (item) => item.stock && item.stock !== 999 && item.quantity > item.stock
    );
    if (outOfStockItems.length > 0) {
      const itemNames = outOfStockItems
        .map((i) => `${i.name} (max ${i.stock})`)
        .join(", ");
      toast({
        title: "Insufficient stock",
        description: `Please reduce quantities: ${itemNames}`,
        variant: "destructive",
      });
      return;
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

      // Set flag to prevent cart reloading
      setJustCompletedCheckout(true);

      // Immediately clear cart and reset everything
      console.log("🔄 Clearing cart after successful checkout...");

      // Force clear cart state immediately
      forceClearCartState();

      // Also clear cart from backend
      await clearCart();
      resetCartCompletely();

      // Reset form
      setDeliveryAddress("");
      setContactNumber("");
      setPaymentMethod("credit_card");
      setCardNumber("");
      setCardHolderName("");
      setExpiryMonth("");
      setExpiryYear("");
      setCvv("");

      // Force re-render by updating cart key
      setCartKey((prev) => prev + 1);

      console.log("✅ Cart clearing completed, navigating...");

      // Navigate immediately without delay
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

  const handleClearCart = () => {
    clearCart();
    resetCartCompletely();
    setCartKey((prev) => prev + 1); // Force re-render
    toast({
      title: "Cart Cleared",
      description: "All items have been removed from your cart.",
    });
  };

  const handleRemoveItem = (itemId: string) => {
    removeFromCart(itemId);
    toast({
      title: "Item Removed",
      description: "Item has been removed from your cart.",
    });
  };

  const handleUpdateQuantity = (itemId: string, newQuantity: number) => {
    if (newQuantity <= 0) {
      handleRemoveItem(itemId);
      return;
    }
    const cartItem = cart.find((i) => i.id === itemId);
    if (
      cartItem &&
      cartItem.stock &&
      cartItem.stock !== 999 &&
      newQuantity > cartItem.stock
    ) {
      toast({
        title: "Quantity limited by stock",
        description: `Only ${cartItem.stock} in stock for ${cartItem.name}`,
        variant: "destructive",
      });
      updateCartQuantity(itemId, cartItem.stock);
      return;
    }
    updateCartQuantity(itemId, newQuantity);
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
              <CardHeader className="flex flex-row items-center justify-between">
                <CardTitle>Order items ({cart.length})</CardTitle>
                {cart.length > 0 && (
                  <AlertDialog>
                    <AlertDialogTrigger asChild>
                      <Button
                        variant="outline"
                        size="sm"
                        className="text-destructive hover:text-destructive"
                      >
                        <RotateCcw className="h-4 w-4 mr-2" />
                        Clear Cart
                      </Button>
                    </AlertDialogTrigger>
                    <AlertDialogContent>
                      <AlertDialogHeader>
                        <AlertDialogTitle>Clear Cart</AlertDialogTitle>
                        <AlertDialogDescription>
                          Are you sure you want to remove all items from your
                          cart? This action cannot be undone.
                        </AlertDialogDescription>
                      </AlertDialogHeader>
                      <AlertDialogFooter>
                        <AlertDialogCancel>Cancel</AlertDialogCancel>
                        <AlertDialogAction
                          onClick={handleClearCart}
                          className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
                        >
                          Clear Cart
                        </AlertDialogAction>
                      </AlertDialogFooter>
                    </AlertDialogContent>
                  </AlertDialog>
                )}
              </CardHeader>
              <CardContent className="space-y-4" key={cartKey}>
                {cart.map((item, index) => (
                  <div
                    key={`${item.id}-${item.backendId || index}`}
                    className="flex items-center justify-between p-4 border rounded-lg hover:bg-muted/50 transition-colors"
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

                        {/* Personalization Details */}
                        {item.personalizationDetails &&
                          Object.keys(item.personalizationDetails).length >
                            0 && (
                            <div className="mt-2 p-2 bg-muted/50 rounded-md">
                              <p className="text-xs font-medium text-muted-foreground mb-1">
                                Personalization:
                              </p>
                              <div className="text-xs space-y-1">
                                {item.personalizationDetails.occasion && (
                                  <div>
                                    • Occasion:{" "}
                                    {item.personalizationDetails.occasion}
                                  </div>
                                )}

                                {/* New JSON structure fields */}
                                {item.personalizationDetails.teddy &&
                                  item.personalizationDetails.teddy
                                    .included && (
                                    <div>
                                      • Teddy:{" "}
                                      {item.personalizationDetails.teddy.type ||
                                        "Bear"}
                                      {item.personalizationDetails.teddy
                                        .color &&
                                        ` (${item.personalizationDetails.teddy.color})`}
                                    </div>
                                  )}

                                {item.personalizationDetails.flowers &&
                                  item.personalizationDetails.flowers.count && (
                                    <div>
                                      • Flowers:{" "}
                                      {
                                        item.personalizationDetails.flowers
                                          .count
                                      }
                                      {item.personalizationDetails.flowers
                                        .color &&
                                        ` (${item.personalizationDetails.flowers.color})`}
                                    </div>
                                  )}

                                {item.personalizationDetails.wrapping_paper && (
                                  <div>
                                    • Wrapping:{" "}
                                    {item.personalizationDetails.wrapping_paper}
                                  </div>
                                )}

                                {item.personalizationDetails.soft_toys && (
                                  <div>
                                    • Soft Toys:{" "}
                                    {item.personalizationDetails.soft_toys}
                                  </div>
                                )}

                                {item.personalizationDetails.felt_design && (
                                  <div>
                                    • Custom Design:{" "}
                                    {item.personalizationDetails.felt_design}
                                  </div>
                                )}

                                {item.personalizationDetails.custom_message && (
                                  <div>
                                    • Message:{" "}
                                    {item.personalizationDetails.custom_message}
                                  </div>
                                )}

                                {/* Legacy fields for backward compatibility */}
                                {item.personalizationDetails.flowersCount &&
                                  !item.personalizationDetails.flowers && (
                                    <div>
                                      • Flowers:{" "}
                                      {item.personalizationDetails.flowersCount}
                                    </div>
                                  )}
                                {item.personalizationDetails.flowersColor &&
                                  !item.personalizationDetails.flowers && (
                                    <div>
                                      • Color:{" "}
                                      {item.personalizationDetails.flowersColor}
                                    </div>
                                  )}
                                {item.personalizationDetails.teddy &&
                                  typeof item.personalizationDetails.teddy ===
                                    "string" && (
                                    <div>
                                      • Teddy:{" "}
                                      {item.personalizationDetails.teddy}
                                    </div>
                                  )}
                                {item.personalizationDetails.teddyType &&
                                  !item.personalizationDetails.teddy && (
                                    <div>
                                      • Teddy Type:{" "}
                                      {item.personalizationDetails.teddyType}
                                    </div>
                                  )}
                                {item.personalizationDetails.wrappingPaper &&
                                  !item.personalizationDetails
                                    .wrapping_paper && (
                                    <div>
                                      • Wrapping:{" "}
                                      {
                                        item.personalizationDetails
                                          .wrappingPaper
                                      }
                                    </div>
                                  )}
                                {item.personalizationDetails.feltDesign &&
                                  !item.personalizationDetails.felt_design && (
                                    <div>
                                      • Custom Design:{" "}
                                      {item.personalizationDetails.feltDesign}
                                    </div>
                                  )}
                                {item.personalizationDetails.softToys &&
                                  !item.personalizationDetails.soft_toys && (
                                    <div>
                                      • Soft Toys:{" "}
                                      {item.personalizationDetails.softToys}
                                    </div>
                                  )}
                              </div>
                            </div>
                          )}

                        <div className="flex items-center gap-2 mt-2">
                          <Badge variant="outline" className="text-xs">
                            Rs {item.price.toFixed(2)} each
                          </Badge>
                          {getCurrentExtraPrice(item) > 0 && (
                            <Badge variant="secondary" className="text-xs">
                              +Rs {getCurrentExtraPrice(item).toFixed(2)} extras
                            </Badge>
                          )}
                          {item.stock && item.stock !== 999 && (
                            <Badge variant="secondary" className="text-xs">
                              {item.stock} in stock
                            </Badge>
                          )}
                        </div>
                      </div>
                    </div>

                    <div className="flex items-center gap-4">
                      {/* Quantity Controls */}
                      <div className="flex items-center gap-2">
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() =>
                            handleUpdateQuantity(item.id, item.quantity - 1)
                          }
                          disabled={item.quantity <= 1}
                          className="h-8 w-8 p-0"
                        >
                          <Minus className="h-3 w-3" />
                        </Button>
                        <div className="flex items-center gap-1">
                          <Input
                            type="number"
                            min="1"
                            max={
                              item.stock && item.stock !== 999
                                ? item.stock
                                : undefined
                            }
                            value={item.quantity}
                            onChange={(e) => {
                              const newQuantity = parseInt(e.target.value) || 1;
                              handleUpdateQuantity(item.id, newQuantity);
                            }}
                            className="w-16 h-8 text-center text-sm"
                          />
                        </div>
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() =>
                            handleUpdateQuantity(item.id, item.quantity + 1)
                          }
                          disabled={
                            item.stock && item.stock !== 999
                              ? item.quantity >= item.stock
                              : false
                          }
                          className="h-8 w-8 p-0"
                        >
                          <Plus className="h-3 w-3" />
                        </Button>
                      </div>

                      {/* Price */}
                      <div className="text-right min-w-[100px]">
                        <p className="font-semibold text-lg">
                          Rs
                          {(
                            (item.price + getCurrentExtraPrice(item)) *
                            item.quantity
                          ).toFixed(2)}
                        </p>
                        {item.quantity > 1 && (
                          <p className="text-xs text-muted-foreground">
                            Rs{" "}
                            {(item.price + getCurrentExtraPrice(item)).toFixed(
                              2
                            )}{" "}
                            × {item.quantity}
                          </p>
                        )}
                        {getCurrentExtraPrice(item) > 0 && (
                          <p className="text-xs text-muted-foreground">
                            Base: Rs {item.price.toFixed(2)} + Rs
                            {getCurrentExtraPrice(item).toFixed(2)} extras
                          </p>
                        )}
                      </div>

                      {/* Remove Button */}
                      <AlertDialog>
                        <AlertDialogTrigger asChild>
                          <Button
                            variant="ghost"
                            size="sm"
                            className="text-destructive hover:text-destructive hover:bg-destructive/10 h-8 w-8 p-0"
                            title="Remove item"
                          >
                            <X className="h-4 w-4" />
                          </Button>
                        </AlertDialogTrigger>
                        <AlertDialogContent>
                          <AlertDialogHeader>
                            <AlertDialogTitle>Remove Item</AlertDialogTitle>
                            <AlertDialogDescription>
                              Are you sure you want to remove "{item.name}" from
                              your cart?
                            </AlertDialogDescription>
                          </AlertDialogHeader>
                          <AlertDialogFooter>
                            <AlertDialogCancel>Cancel</AlertDialogCancel>
                            <AlertDialogAction
                              onClick={() => handleRemoveItem(item.id)}
                              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
                            >
                              Remove
                            </AlertDialogAction>
                          </AlertDialogFooter>
                        </AlertDialogContent>
                      </AlertDialog>
                    </div>
                  </div>
                ))}

                {cart.length === 0 && (
                  <div className="text-center py-8">
                    <ShoppingCart className="h-12 w-12 mx-auto mb-4 text-muted-foreground" />
                    <p className="text-muted-foreground">No items in cart</p>
                  </div>
                )}
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
                  <span>Rs {totals.subtotal.toFixed(2)}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span>Shipping</span>
                  <span>Free</span>
                </div>
                <Separator />
                <div className="flex justify-between font-semibold text-lg">
                  <span>Total</span>
                  <span>Rs {totals.total.toFixed(2)}</span>
                </div>
                <Button
                  className="w-full"
                  onClick={handlePlaceOrder}
                  disabled={isPlacingOrder}
                >
                  {isPlacingOrder ? "Placing Order..." : "Place Order"}
                </Button>

                {cart.length > 0 && (
                  <AlertDialog>
                    <AlertDialogTrigger asChild>
                      <Button
                        variant="outline"
                        className="w-full text-destructive hover:text-destructive"
                      >
                        <RotateCcw className="h-4 w-4 mr-2" />
                        Clear Cart
                      </Button>
                    </AlertDialogTrigger>
                    <AlertDialogContent>
                      <AlertDialogHeader>
                        <AlertDialogTitle>Clear Cart</AlertDialogTitle>
                        <AlertDialogDescription>
                          Are you sure you want to remove all items from your
                          cart? This action cannot be undone.
                        </AlertDialogDescription>
                      </AlertDialogHeader>
                      <AlertDialogFooter>
                        <AlertDialogCancel>Cancel</AlertDialogCancel>
                        <AlertDialogAction
                          onClick={handleClearCart}
                          className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
                        >
                          Clear Cart
                        </AlertDialogAction>
                      </AlertDialogFooter>
                    </AlertDialogContent>
                  </AlertDialog>
                )}
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Checkout;
