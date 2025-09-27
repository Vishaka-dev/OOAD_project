import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { X } from "lucide-react";

interface CategoryFilterProps {
  categories: Array<{
    id: string;
    name: string;
    description: string;
    productCount: number;
  }>;
  selectedCategory: number | null;
  onCategoryChange: (categoryId: number | null) => void;
}

export function CategoryFilter({
  categories,
  selectedCategory,
  onCategoryChange,
}: CategoryFilterProps) {
  return (
    <div className="space-y-3">
      <h3 className="font-medium text-sm">Categories</h3>
      <div className="space-y-2">
        <Button
          variant={selectedCategory === null ? "default" : "ghost"}
          size="sm"
          onClick={() => onCategoryChange(null)}
          className="w-full justify-start"
        >
          All Categories
        </Button>

        {categories.map((category) => (
          <Button
            key={category.id}
            variant={
              selectedCategory === parseInt(category.id) ? "default" : "ghost"
            }
            size="sm"
            onClick={() => onCategoryChange(parseInt(category.id))}
            className="w-full justify-between"
          >
            <span className="truncate">{category.name}</span>
            <Badge variant="secondary" className="ml-2 text-xs">
              {category.productCount}
            </Badge>
          </Button>
        ))}
      </div>
    </div>
  );
}
