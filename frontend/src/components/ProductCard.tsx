import { Star, ShoppingCart, Heart, Settings2 } from "lucide-react";
import { Card, CardContent, CardFooter } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Product, UIProduct } from "@/types/product";
import { useStore } from "@/hooks/useStore";
import { useEffect, useState } from "react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Input } from "@/components/ui/input";
import { apiClient } from "@/lib/api";
import {
  PersonalizationOptionDTO,
  PersonalizationDetails,
} from "@/types/product";
import {
  convertToNewFormat,
  calculateExtraCost,
} from "@/lib/personalization-utils";

interface ProductCardProps {
  product: Product | UIProduct;
  onViewDetails?: (product: Product | UIProduct) => void;
}

export function ProductCard({ product, onViewDetails }: ProductCardProps) {
  const { addToCart } = useStore();
  const [open, setOpen] = useState(false);
  const [quantity, setQuantity] = useState(1);
  const [details, setDetails] = useState<PersonalizationDetails>({});
  const [options, setOptions] = useState<PersonalizationOptionDTO[] | null>(
    null
  );
  const [loadingOptions, setLoadingOptions] = useState(false);

  // Helper functions to handle both Product and UIProduct types
  const productId =
    "productId" in product ? product.productId : parseInt(product.id);
  const productName = product.name;
  const productImage = "imageUrl" in product ? product.imageUrl : product.image;
  const productPrice = product.price;
  const productDescription = product.description;
  const productStock =
    "stockQuantity" in product ? product.stockQuantity : product.stock;
  const productRating = "rating" in product ? product.rating : 4.5;
  const productReviews = "reviews" in product ? product.reviews : 0;
  const productSize = "size" in product ? product.size : "Medium";
  const productCategory =
    "categoryName" in product ? product.categoryName : product.category;
  const originalPrice =
    "originalPrice" in product ? product.originalPrice : undefined;

  const handleAddToCart = async (e: React.MouseEvent) => {
    e.stopPropagation();
    try {
      // Convert to UIProduct format for the store
      const uiProduct: UIProduct = {
        id: productId.toString(),
        name: productName,
        description: productDescription,
        price: productPrice,
        originalPrice: originalPrice,
        image: productImage,
        category: productCategory,
        size: productSize,
        stock: productStock,
        rating: productRating,
        reviews: productReviews,
      };
      await addToCart(uiProduct, 1);
    } catch (error) {
      // Show error message to user
      alert(
        error instanceof Error ? error.message : "Failed to add item to cart"
      );
    }
  };

  const handleViewDetails = () => {
    onViewDetails?.(product);
  };

  const handlePersonalizedAdd = async () => {
    if (Object.keys(details).length === 0) {
      alert("Please select at least one personalization option");
      return;
    }

    try {
      // Convert to UIProduct format for the store
      const uiProduct: UIProduct = {
        id: productId.toString(),
        name: productName,
        description: productDescription,
        price: productPrice,
        originalPrice: originalPrice,
        image: productImage,
        category: productCategory,
        size: productSize,
        stock: productStock,
        rating: productRating,
        reviews: productReviews,
      };

      // Convert legacy format to new JSON structure
      const newFormatDetails = convertToNewFormat(details);

      // Calculate extra price using the new structure
      const extraPrice = calculateExtraCost(newFormatDetails);

      await addToCart(uiProduct, quantity, newFormatDetails, extraPrice);
      setOpen(false);
      setQuantity(1);
      setDetails({});
    } catch (error) {
      // Show error message to user
      alert(
        error instanceof Error
          ? error.message
          : "Failed to add personalized item to cart"
      );
    }
  };

  useEffect(() => {
    if (!open) return;
    (async () => {
      try {
        setLoadingOptions(true);
        // TODO: Add getPersonalizationOptions to apiClient
        // const res = await apiClient.getPersonalizationOptions(productId);
        setOptions([]);
      } catch (e) {
        setOptions([]);
      } finally {
        setLoadingOptions(false);
      }
    })();
  }, [open, productId]);

  const handleOpenPersonalize = async (e: React.MouseEvent) => {
    e.stopPropagation();
    setOpen(true);
    try {
      setLoadingOptions(true);
      // TODO: Add getPersonalizationOptions to apiClient
      // const res = await apiClient.getPersonalizationOptions(productId);
      setOptions([]);
    } catch (e) {
      setOptions([]);
    } finally {
      setLoadingOptions(false);
    }
  };

  return (
    <Card className="group cursor-pointer overflow-hidden border-border transition-all duration-300 hover:shadow-lg hover:shadow-teddy-300/20 hover:-translate-y-1">
      <div className="relative aspect-square overflow-hidden bg-gradient-to-br from-teddy-50 to-pink-50">
        <img
          src={productImage}
          alt={productName}
          className="h-full w-full object-cover transition-transform duration-300 group-hover:scale-105"
          onClick={handleViewDetails}
        />
        {originalPrice && (
          <Badge className="absolute top-2 left-2 bg-pink-500 text-primary-foreground">
            Sale
          </Badge>
        )}
        <Button
          variant="ghost"
          size="icon"
          className="absolute top-2 right-2 bg-background/80 backdrop-blur-sm hover:bg-background"
        >
          <Heart className="h-4 w-4" />
        </Button>
        {productStock <= 5 && productStock > 0 && (
          <Badge className="absolute bottom-2 left-2 bg-destructive text-destructive-foreground">
            Only {productStock} left
          </Badge>
        )}
        {productStock === 0 && (
          <Badge className="absolute bottom-2 left-2 bg-muted text-muted-foreground">
            Out of Stock
          </Badge>
        )}
      </div>

      <CardContent className="p-3 sm:p-4" onClick={handleViewDetails}>
        <div className="space-y-1.5">
          <h3 className="text-sm sm:text-base font-semibold text-foreground group-hover:text-primary transition-colors line-clamp-1">
            {productName}
          </h3>
          <p className="text-xs sm:text-sm text-muted-foreground line-clamp-2">
            {productDescription}
          </p>

          <div className="flex items-center space-x-1">
            {[...Array(5)].map((_, i) => (
              <Star
                key={i}
                className={`h-3 w-3 ${
                  i < Math.floor(productRating)
                    ? "fill-yellow-400 text-yellow-400"
                    : "fill-muted text-muted"
                }`}
              />
            ))}
            <span className="text-xs text-muted-foreground ml-1">
              ({productReviews})
            </span>
          </div>

          <div className="flex items-center space-x-2">
            <span className="text-base sm:text-lg font-bold text-foreground">
              Rs {productPrice.toFixed(2)}
            </span>
            {originalPrice && (
              <span className="text-xs sm:text-sm text-muted-foreground line-through">
                Rs {originalPrice.toFixed(2)}
              </span>
            )}
          </div>

          <div className="flex items-center justify-between gap-1">
            <Badge variant="outline" className="text-xs px-1.5 py-0.5">
              {productSize}
            </Badge>
            <Badge
              variant="outline"
              className="text-xs px-1.5 py-0.5 truncate max-w-[100px]"
            >
              {productCategory}
            </Badge>
          </div>
        </div>
      </CardContent>

      <CardFooter className="p-2 sm:p-3 pt-0 flex flex-col sm:flex-row gap-2">
        <Button
          className="w-full sm:flex-1 text-xs sm:text-sm h-8 sm:h-9"
          variant={productStock === 0 ? "outline" : "teddy"}
          disabled={productStock === 0}
          onClick={handleAddToCart}
          size="sm"
        >
          <ShoppingCart className="mr-1 h-3 w-3" />
          {productStock === 0 ? "Out of Stock" : "Add as-is"}
        </Button>
        <Dialog open={open} onOpenChange={setOpen}>
          <DialogTrigger asChild>
            <Button
              variant="outline"
              className="w-full sm:flex-1 text-xs sm:text-sm h-8 sm:h-9"
              disabled={productStock === 0}
              onClick={handleOpenPersonalize}
              size="sm"
            >
              <Settings2 className="mr-1 h-3 w-3" /> Personalize
            </Button>
          </DialogTrigger>
          <DialogContent
            aria-describedby={undefined}
            onClick={(e) => e.stopPropagation()}
            className="max-w-[95vw] sm:max-w-lg max-h-[90vh] overflow-y-auto"
          >
            <DialogHeader>
              <DialogTitle className="text-base sm:text-lg">
                Personalize {productName}
              </DialogTitle>
            </DialogHeader>
            <div className="grid gap-3 sm:gap-4 py-2">
              {/* Example: fetched options preview (optional) */}
              {loadingOptions ? (
                <div className="text-sm text-muted-foreground">
                  Loading personalization options…
                </div>
              ) : options && options.length > 0 ? (
                <div className="text-xs text-muted-foreground">
                  Available options:{" "}
                  {options
                    .map((o) => o.usiType || o.color || o.massage)
                    .filter(Boolean)
                    .join(", ")}
                </div>
              ) : null}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 sm:gap-4">
                <div className="space-y-2">
                  <label className="text-xs sm:text-sm font-medium">
                    Occasion
                  </label>
                  <Select
                    onValueChange={(v) =>
                      setDetails((d: PersonalizationDetails) => ({
                        ...d,
                        occasion: v,
                      }))
                    }
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select occasion" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="Graduation">
                        Graduation bouquet
                      </SelectItem>
                      <SelectItem value="Birthday">Birthday bouquet</SelectItem>
                      <SelectItem value="Valentine">
                        Valentine bouquet
                      </SelectItem>
                      <SelectItem value="Mini">
                        Mini bouquet (1 flower)
                      </SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-2">
                  <label className="text-xs sm:text-sm font-medium">
                    Flowers
                  </label>
                  <Select
                    onValueChange={(v) =>
                      setDetails((d: PersonalizationDetails) => ({
                        ...d,
                        flowersCount: v,
                      }))
                    }
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select count" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="7">7 flowers</SelectItem>
                      <SelectItem value="10">10 flowers</SelectItem>
                      <SelectItem value="1">1 flower</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 sm:gap-4">
                <div className="space-y-2">
                  <label className="text-xs sm:text-sm font-medium">
                    Flowers color
                  </label>
                  <Select
                    onValueChange={(v) =>
                      setDetails((d: PersonalizationDetails) => ({
                        ...d,
                        flowersColor: v,
                      }))
                    }
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select color" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="red">Red</SelectItem>
                      <SelectItem value="white">White</SelectItem>
                      <SelectItem value="yellow">Yellow</SelectItem>
                      <SelectItem value="purple">Purple</SelectItem>
                      <SelectItem value="pink">Pink</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-2">
                  <label className="text-xs sm:text-sm font-medium">
                    Wrapping Paper
                  </label>
                  <Select
                    onValueChange={(v) =>
                      setDetails((d: PersonalizationDetails) => ({
                        ...d,
                        wrappingPaper: v,
                      }))
                    }
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select type" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="printed">Printed</SelectItem>
                      <SelectItem value="plain">Plain (Dyed)</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 sm:gap-4">
                <div className="space-y-2">
                  <label className="text-xs sm:text-sm font-medium">
                    Include Teddy
                  </label>
                  <Select
                    onValueChange={(v) =>
                      setDetails((d: PersonalizationDetails) => ({
                        ...d,
                        teddy: v,
                      }))
                    }
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="With/Without" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="with">With teddy</SelectItem>
                      <SelectItem value="without">Without teddy</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-2">
                  <label className="text-xs sm:text-sm font-medium">
                    Teddy Type
                  </label>
                  <Select
                    onValueChange={(v) =>
                      setDetails((d: PersonalizationDetails) => ({
                        ...d,
                        teddyType: v,
                      }))
                    }
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select type" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="handmade">
                        Handmade Graduation Teddy
                      </SelectItem>
                      <SelectItem value="fluffy">
                        Fluffy Premium Graduation Teddy
                      </SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 sm:gap-4">
                <div className="space-y-2">
                  <label className="text-xs sm:text-sm font-medium">
                    Teddy Color
                  </label>
                  <Select
                    onValueChange={(v) =>
                      setDetails((d: PersonalizationDetails) => ({
                        ...d,
                        teddyColor: v,
                      }))
                    }
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select color" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="white">White</SelectItem>
                      <SelectItem value="cream">Cream</SelectItem>
                      <SelectItem value="pink">Pink</SelectItem>
                      <SelectItem value="yellow">Yellow</SelectItem>
                      <SelectItem value="light yellow">Light Yellow</SelectItem>
                      <SelectItem value="blue">Blue</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-2">
                  <label className="text-xs sm:text-sm font-medium">
                    Felt Design (Teddy)
                  </label>
                  <Input
                    placeholder="Describe felt design"
                    className="text-sm"
                    onChange={(e) =>
                      setDetails((d: PersonalizationDetails) => ({
                        ...d,
                        feltDesign: e.target.value,
                      }))
                    }
                  />
                </div>
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 sm:gap-4">
                <div className="space-y-2">
                  <label className="text-xs sm:text-sm font-medium">
                    Soft toys
                  </label>
                  <Select
                    onValueChange={(v) =>
                      setDetails((d: PersonalizationDetails) => ({
                        ...d,
                        softToys: v,
                      }))
                    }
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select option" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="dolls">Dolls</SelectItem>
                      <SelectItem value="animal">Animal</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-2">
                  <label className="text-xs sm:text-sm font-medium">
                    Quantity
                  </label>
                  <Input
                    type="number"
                    min={1}
                    value={quantity}
                    className="text-sm"
                    onChange={(e) =>
                      setQuantity(parseInt(e.target.value || "1"))
                    }
                  />
                </div>
              </div>
              <div className="flex flex-col-reverse sm:flex-row justify-end gap-2 pt-2">
                <Button
                  variant="outline"
                  onClick={() => setOpen(false)}
                  className="w-full sm:w-auto"
                >
                  Cancel
                </Button>
                <Button
                  onClick={handlePersonalizedAdd}
                  className="w-full sm:w-auto"
                >
                  Add Personalized
                </Button>
              </div>
            </div>
          </DialogContent>
        </Dialog>
      </CardFooter>
    </Card>
  );
}
