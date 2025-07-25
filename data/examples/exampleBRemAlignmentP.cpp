#ifdef Feature1
    int x = 5;
#ifdef FeatureB
    int b = 1234;
#endif
    x *= x;
#endif
#ifdef Feature2
    y *= 10;
#endif
