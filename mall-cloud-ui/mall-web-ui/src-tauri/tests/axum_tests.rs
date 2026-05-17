#[test]
fn fails_when_in_wrong_dir() {
    println!("hello world!");

    let a = 3;
    let b = 1 + 2;
    // 使用断言判断结果的一致性
    assert_eq!(a, b);

    assert_eq!(a, b, "we are testing addition with {} and {}", a, b);
}
