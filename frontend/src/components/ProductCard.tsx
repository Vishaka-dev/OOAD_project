import { Star, ShoppingCart, Heart } from 'lucide-react';
import { Card, CardContent, CardFooter } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Product } from '@/types/product';
import { useStore } from '@/hooks/useStore';

interface ProductCardProps {
  product: Product;
  onViewDetails?: (product: Product) => void;
}

export function ProductCard({ product, onViewDetails }: ProductCardProps) {
  const { addToCart } = useStore();

  const handleAddToCart = (e: React.MouseEvent) => {
    e.stopPropagation();
    addToCart(product);
  };

  const handleViewDetails = () => {
    onViewDetails?.(product);
  };

  return (
    <Card className="group cursor-pointer overflow-hidden border-border transition-all duration-300 hover:shadow-lg hover:shadow-teddy-300/20 hover:-translate-y-1">
      <div className="relative aspect-square overflow-hidden bg-gradient-to-br from-teddy-50 to-pink-50">
        <img
          src={product.image}
          alt={product.name}
          className="h-full w-full object-cover transition-transform duration-300 group-hover:scale-105"
          onClick={handleViewDetails}
        />
        {product.originalPrice && (
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
        {product.stock <= 5 && product.stock > 0 && (
          <Badge className="absolute bottom-2 left-2 bg-destructive text-destructive-foreground">
            Only {product.stock} left
          </Badge>
        )}
        {product.stock === 0 && (
          <Badge className="absolute bottom-2 left-2 bg-muted text-muted-foreground">
            Out of Stock
          </Badge>
        )}
      </div>

      <CardContent className="p-4" onClick={handleViewDetails}>
        <div className="space-y-2">
          <h3 className="font-semibold text-foreground group-hover:text-primary transition-colors">
            {product.name}
          </h3>
          <p className="text-sm text-muted-foreground line-clamp-2">
            {product.description}
          </p>
          
          <div className="flex items-center space-x-1">
            {[...Array(5)].map((_, i) => (
              <Star
                key={i}
                className={`h-3 w-3 ${
                  i < Math.floor(product.rating)
                    ? 'fill-yellow-400 text-yellow-400'
                    : 'fill-muted text-muted'
                }`}
              />
            ))}
            <span className="text-xs text-muted-foreground ml-1">
              ({product.reviews})
            </span>
          </div>

          <div className="flex items-center space-x-2">
            <span className="text-lg font-bold text-foreground">
              ${product.price.toFixed(2)}
            </span>
            {product.originalPrice && (
              <span className="text-sm text-muted-foreground line-through">
                ${product.originalPrice.toFixed(2)}
              </span>
            )}
          </div>

          <div className="flex items-center justify-between">
            <Badge variant="outline" className="text-xs">
              {product.size}
            </Badge>
            <Badge variant="outline" className="text-xs">
              {product.category}
            </Badge>
          </div>
        </div>
      </CardContent>

      <CardFooter className="p-4 pt-0">
        <Button
          className="w-full"
          variant={product.stock === 0 ? "outline" : "teddy"}
          disabled={product.stock === 0}
          onClick={handleAddToCart}
        >
          <ShoppingCart className="mr-2 h-4 w-4" />
          {product.stock === 0 ? 'Out of Stock' : 'Add to Cart'}
        </Button>
      </CardFooter>
    </Card>
  );
}