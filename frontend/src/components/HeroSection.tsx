import { ArrowRight, Star } from "lucide-react";
import { Button } from "@/components/ui/button";
import heroImage from "@/assets/hero-teddy-store.jpg";

export function HeroSection() {
  return (
    <section className="relative min-h-[60vh] sm:min-h-[70vh] lg:min-h-[80vh] flex items-center justify-center overflow-hidden bg-gradient-to-br from-teddy-50 via-pink-50 to-teddy-100">
      <div className="absolute inset-0 bg-gradient-to-r from-background/20 to-transparent z-10" />

      <div className="container relative z-20 grid lg:grid-cols-2 gap-8 sm:gap-12 items-center max-w-screen-xl px-4 py-8 sm:py-12">
        {/* Text Content */}
        <div className="space-y-6 sm:space-y-8">
          <div className="space-y-3 sm:space-y-4">
            <div className="flex items-center space-x-2">
              <div className="flex items-center">
                {[...Array(5)].map((_, i) => (
                  <Star
                    key={i}
                    className="h-3 w-3 sm:h-4 sm:w-4 fill-yellow-400 text-yellow-400"
                  />
                ))}
              </div>
              <span className="text-xs sm:text-sm text-muted-foreground">
                Loved by 10,000+ customers
              </span>
            </div>

            <h1 className="text-3xl sm:text-4xl md:text-5xl lg:text-6xl font-bold leading-tight">
              <span className="bg-gradient-to-r from-teddy-700 to-pink-600 bg-clip-text text-transparent">
                Cuddle & Love
              </span>
              <br />
              <span className="text-foreground">Every Day</span>
            </h1>

            <p className="text-sm sm:text-base lg:text-lg text-muted-foreground max-w-md">
              Discover our collection of premium teddy bears and plush toys.
              Perfect companions for every age, crafted with love and designed
              to last a lifetime.
            </p>
          </div>

          <div className="flex flex-col sm:flex-row gap-3 sm:gap-4">
            <Button variant="hero" size="xl" className="group w-full sm:w-auto">
              Shop Now
              <ArrowRight className="ml-2 h-4 w-4 sm:h-5 sm:w-5 transition-transform group-hover:translate-x-1" />
            </Button>
            <Button variant="soft" size="xl" className="w-full sm:w-auto">
              View Collection
            </Button>
          </div>

          <div className="grid grid-cols-3 gap-4 sm:gap-6 pt-6 sm:pt-8">
            <div className="text-center">
              <div className="text-xl sm:text-2xl font-bold text-teddy-700">
                500+
              </div>
              <div className="text-xs sm:text-sm text-muted-foreground">
                Happy Bears
              </div>
            </div>
            <div className="text-center">
              <div className="text-xl sm:text-2xl font-bold text-teddy-700">
                10k+
              </div>
              <div className="text-xs sm:text-sm text-muted-foreground">
                Customers
              </div>
            </div>
            <div className="text-center">
              <div className="text-xl sm:text-2xl font-bold text-teddy-700">
                99%
              </div>
              <div className="text-xs sm:text-sm text-muted-foreground">
                Satisfaction
              </div>
            </div>
          </div>
        </div>

        {/* Hero Image */}
        <div className="relative mt-8 lg:mt-0">
          <div className="absolute inset-0 bg-gradient-to-br from-teddy-200/20 to-pink-200/20 rounded-2xl sm:rounded-3xl transform rotate-6" />
          <div className="relative bg-white rounded-2xl sm:rounded-3xl overflow-hidden shadow-2xl shadow-teddy-300/30">
            <img
              src={heroImage}
              alt="TeddyLove Collection"
              className="w-full h-auto object-cover"
            />
            <div className="absolute inset-0 bg-gradient-to-t from-teddy-900/20 to-transparent" />
          </div>

          {/* Floating Elements - Hidden on very small screens */}
          <div className="hidden sm:block absolute -top-3 sm:-top-4 -right-3 sm:-right-4 bg-pink-500 text-primary-foreground rounded-full p-2 sm:p-3 shadow-lg animate-bounce">
            <span className="text-xl sm:text-2xl">🧸</span>
          </div>
          <div className="hidden sm:block absolute -bottom-3 sm:-bottom-4 -left-3 sm:-left-4 bg-teddy-600 text-primary-foreground rounded-full p-2 sm:p-3 shadow-lg animate-pulse">
            <span className="text-xl sm:text-2xl">❤️</span>
          </div>
        </div>
      </div>
    </section>
  );
}
