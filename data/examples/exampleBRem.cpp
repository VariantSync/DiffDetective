#ifdef Feature1
    int x = 5;
#ifdef Feature4
    int y = 10;
#endif
    x *= x;
#ifdef Feature4
    x += 5;
#endif
#endif
