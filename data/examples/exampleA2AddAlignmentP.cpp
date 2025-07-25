#ifdef Feature1
    int x = 5;
#ifdef Feature2
    int y = 10;
#endif
    x *= x;
#endif
#ifdef Feature2
    y *= 10;
#endif
