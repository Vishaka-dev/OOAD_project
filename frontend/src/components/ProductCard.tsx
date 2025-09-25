import { Star, ShoppingCart, Heart, Settings2 } from 'lucide-react';
import { Card, CardContent, CardFooter } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Product } from '@/types/product';
import { useStore } from '@/hooks/useStore';
import { useEffect, useState } from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Input } from '@/components/ui/input';
import { apiClient, PersonalizationOptionDTO } from '@/lib/api';

interface ProductCardProps {
  product: Product;
  onViewDetails?: (product: Product) => void;
}

export function ProductCard({ product, onViewDetails }: ProductCardProps) {
  const { addToCart } = useStore();
  const [open, setOpen] = useState(false);
  const [quantity, setQuantity] = useState(1);
  const [details, setDetails] = useState<any>({});
  const [options, setOptions] = useState<PersonalizationOptionDTO[] | null>(null);
  const [loadingOptions, setLoadingOptions] = useState(false);

  const handleAddToCart = (e: React.MouseEvent) => {
    e.stopPropagation();
    addToCart(product, 1);
  };

  const handleViewDetails = () => {
    onViewDetails?.(product);
  };

  const handlePersonalizedAdd = () => {
    addToCart(product, quantity, details);
    setOpen(false);
    setQuantity(1);
    setDetails({});
  };

  useEffect(() => {
    if (!open) return;
    (async () => {
      try {
        setLoadingOptions(true);
        const res = await apiClient.getPersonalizationOptions(parseInt(product.id));
        setOptions(res);
      } catch (e) {
        setOptions([]);
      } finally {
        setLoadingOptions(false);
      }
    })();
  }, [open, product.id]);

  const handleOpenPersonalize = async (e: React.MouseEvent) => {
    e.stopPropagation();
    setOpen(true);
    try {
      setLoadingOptions(true);
      const res = await apiClient.getPersonalizationOptions(parseInt(product.id));
      setOptions(res);
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

      <CardFooter className="p-4 pt-0 flex gap-2">
        <Button
          className="flex-1"
          variant={product.stock === 0 ? "outline" : "teddy"}
          disabled={product.stock === 0}
          onClick={handleAddToCart}
        >
          <ShoppingCart className="mr-2 h-4 w-4" />
          {product.stock === 0 ? 'Out of Stock' : 'Add as-is'}
        </Button>
        <Dialog open={open} onOpenChange={setOpen}>
          <DialogTrigger asChild>
            <Button variant="outline" className="flex-1" disabled={product.stock === 0} onClick={handleOpenPersonalize}>
              <Settings2 className="mr-2 h-4 w-4" /> Personalize
            </Button>
          </DialogTrigger>
          <DialogContent aria-describedby={undefined} onClick={(e) => e.stopPropagation()}>
            <DialogHeader>
              <DialogTitle>Personalize {product.name}</DialogTitle>
            </DialogHeader>
            <div className="grid gap-4 py-2">
              {/* Example: fetched options preview (optional) */}
              {loadingOptions ? (
                <div className="text-sm text-muted-foreground">Loading personalization options…</div>
              ) : options && options.length > 0 ? (
                <div className="text-xs text-muted-foreground">Available options: {options.map(o => o.usiType || o.color || o.massage).filter(Boolean).join(', ')}</div>
              ) : null}
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <label className="text-sm">Occasion</label>
                  <Select onValueChange={(v) => setDetails((d: any) => ({ ...d, occasion: v }))}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select occasion" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="Graduation">Graduation bouquet</SelectItem>
                      <SelectItem value="Birthday">Birthday bouquet</SelectItem>
                      <SelectItem value="Valentine">Valentine bouquet</SelectItem>
                      <SelectItem value="Mini">Mini bouquet (1 flower)</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-2">
                  <label className="text-sm">Flowers</label>
                  <Select onValueChange={(v) => setDetails((d: any) => ({ ...d, flowersCount: v }))}>
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
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <label className="text-sm">Flowers color</label>
                  <Select onValueChange={(v) => setDetails((d: any) => ({ ...d, flowersColor: v }))}>
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
                  <label className="text-sm">Wrapping Paper</label>
                  <Select onValueChange={(v) => setDetails((d: any) => ({ ...d, wrappingPaper: v }))}>
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
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <label className="text-sm">Include Teddy</label>
                  <Select onValueChange={(v) => setDetails((d: any) => ({ ...d, teddy: v }))}>
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
                  <label className="text-sm">Teddy Type</label>
                  <Select onValueChange={(v) => setDetails((d: any) => ({ ...d, teddyType: v }))}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select type" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="handmade">Handmade Graduation Teddy</SelectItem>
                      <SelectItem value="fluffy">Fluffy Premium Graduation Teddy</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <label className="text-sm">Teddy Color</label>
                  <Select onValueChange={(v) => setDetails((d: any) => ({ ...d, teddyColor: v }))}>
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
                  <label className="text-sm">Felt Design (Teddy)</label>
                  <Input placeholder="Describe felt design" onChange={(e) => setDetails((d: any) => ({ ...d, feltDesign: e.target.value }))} />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <label className="text-sm">Soft toys</label>
                  <Select onValueChange={(v) => setDetails((d: any) => ({ ...d, softToys: v }))}>
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
                  <label className="text-sm">Quantity</label>
                  <Input type="number" min={1} value={quantity} onChange={(e) => setQuantity(parseInt(e.target.value || '1'))} />
                </div>
              </div>
              <div className="flex justify-end gap-2 pt-2">
                <Button variant="outline" onClick={() => setOpen(false)}>Cancel</Button>
                <Button onClick={handlePersonalizedAdd}>Add Personalized</Button>
              </div>
            </div>
          </DialogContent>
        </Dialog>
      </CardFooter>
    </Card>
  );
}