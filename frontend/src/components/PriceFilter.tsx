import { useState, useEffect } from "react";
import { Slider } from "@/components/ui/slider";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

interface PriceFilterProps {
  priceRange: [number, number];
  onPriceRangeChange: (range: [number, number]) => void;
  min?: number;
  max?: number;
}

export function PriceFilter({
  priceRange,
  onPriceRangeChange,
  min = 0,
  max = 1000,
}: PriceFilterProps) {
  const [localRange, setLocalRange] = useState<[number, number]>(priceRange);

  useEffect(() => {
    setLocalRange(priceRange);
  }, [priceRange]);

  const handleSliderChange = (value: number[]) => {
    const newRange: [number, number] = [value[0], value[1]];
    setLocalRange(newRange);
    onPriceRangeChange(newRange);
  };

  const handleMinChange = (value: string) => {
    const numValue = Math.max(min, Math.min(max, parseInt(value) || min));
    const newRange: [number, number] = [numValue, localRange[1]];
    setLocalRange(newRange);
    onPriceRangeChange(newRange);
  };

  const handleMaxChange = (value: string) => {
    const numValue = Math.max(min, Math.min(max, parseInt(value) || max));
    const newRange: [number, number] = [localRange[0], numValue];
    setLocalRange(newRange);
    onPriceRangeChange(newRange);
  };

  return (
    <div className="space-y-4">
      <h3 className="font-medium text-sm">Price Range</h3>

      <div className="space-y-3">
        <Slider
          value={localRange}
          onValueChange={handleSliderChange}
          min={min}
          max={max}
          step={1}
          className="w-full"
        />

        <div className="flex items-center gap-2">
          <div className="flex-1">
            <Label
              htmlFor="min-price"
              className="text-xs text-muted-foreground"
            >
              Min
            </Label>
            <Input
              id="min-price"
              type="number"
              value={localRange[0]}
              onChange={(e) => handleMinChange(e.target.value)}
              min={min}
              max={max}
              className="text-sm"
            />
          </div>
          <div className="flex-1">
            <Label
              htmlFor="max-price"
              className="text-xs text-muted-foreground"
            >
              Max
            </Label>
            <Input
              id="max-price"
              type="number"
              value={localRange[1]}
              onChange={(e) => handleMaxChange(e.target.value)}
              min={min}
              max={max}
              className="text-sm"
            />
          </div>
        </div>

        <div className="text-xs text-muted-foreground text-center">
          Rs {localRange[0]} - Rs {localRange[1]}
        </div>
      </div>
    </div>
  );
}
